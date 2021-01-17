package cz.minarik.nasapp.ui.articles

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.isInternetAvailable
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.base.network.ApiRequest
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.db.entity.StarredArticleEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.domain.Article
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.data.domain.RSSSourceDTO
import cz.minarik.nasapp.data.domain.exception.GenericException
import cz.minarik.nasapp.data.domain.exception.NoConnectionException
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.Constants
import cz.minarik.nasapp.utils.UniversePrefManager
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.nio.charset.Charset

abstract class GenericArticlesFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val articlesRepository: ArticlesRepository,
    private val starredArticleDao: StarredArticleDao,
    val prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
    private val rssApiService: RssApiService,
) : BaseViewModel() {

    private val parser by lazy {
        Parser.Builder()
            .context(context)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(Constants.articlesCacheExpiration)
            .build()
    }

    private val useRetrofit = false

    private var currentArticleLoadingJob: Job? = null

    //all articles (without active filters)
    private val allArticles: MutableList<ArticleDTO> = mutableListOf()

    //shown articles (may be filtered)
    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    var shouldScrollToTop: Boolean = false

    var searchQuery: String? = null

    init {
        loadArticles(false)
    }

    /**
     * Loads articles from given url into given list.
     *
     * @param url to load articles from
     * @param flushCache whether to force cache flushing (meaning a network call would always happen)
     * @param result the list into which the articles will be added
     */
    private suspend fun loadArticlesFromUrl(
        url: String,
        flushCache: Boolean,
        result: MutableList<Article>
    ) {
        try {
            Timber.i("Loading articles for url: $url")
            if (flushCache) {
                Timber.i("flushing cache for url: $url")
                parser.flushCache(url)
            }
            val source = sourceDao.getByUrl(url)
            source?.let {
                val articles = parser.getChannel(source.url).articles
                articles.forEach {
                    it.sourceUrl = source.url
                    it.sourceName = source.title
                }
                synchronized(this) {
                    result.addAll(articles.map {
                        Article.fromLibrary(it)
                    }.filter { it.isValid }
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    /**
     * Loads articles from given url into given list.
     *
     * @param url to load articles from
     * @param flushCache whether to force cache flushing (meaning a network call would always happen)
     * @param result the list into which the articles will be added
     */
    private suspend fun loadArticlesFromUrlRetrofit(
        url: String,
        flushCache: Boolean,
        result: MutableList<Article>
    ) {
        try {
            Timber.i("Loading articles for url: $url")
            val source = sourceDao.getByUrl(url)
            source?.let {
                val apiResult = ApiRequest.getResult {
                    rssApiService.getRss(source.url)
                }
                val articles = apiResult.data?.items?.map {
                    Article.fromApi(it)
                }
                articles?.forEach {
                    it.sourceUrl = source.url
                    it.sourceName = source.title
                }
                synchronized(this@GenericArticlesFragmentViewModel) {
                    result.addAll(articles ?: mutableListOf())
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun loadArticles(
        force: Boolean = false,
        scrollToTop: Boolean = false,
    ) {
        Timber.i("loading articles, force = $force")
        state.postValue(NetworkState.LOADING)
        currentArticleLoadingJob?.cancel()
        currentArticleLoadingJob = defaultScope.launch {
            try {
                val startTime = System.currentTimeMillis()

                if (prefManager.getArticleFilter() == ArticleFilterType.Starred) {
                    loadStarredArticles()
                }
                if (!context.isInternetAvailable) {
                    state.postValue(NetworkState.Companion.error(NoConnectionException()))
                    return@launch
                }

                val allArticleList = mutableListOf<Article>()

                ensureActive()
                val selectedSource = getSource()

                //todo something more sophisticated like lists etc.
                (selectedSource?.URLs?.indices)?.map {
                    ensureActive()
                    async(Dispatchers.IO) {
                        if (useRetrofit) {
                            loadArticlesFromUrlRetrofit(
                                selectedSource.URLs[it],
                                force,
                                allArticleList
                            )
                        } else {
                            loadArticlesFromUrl(selectedSource.URLs[it], force, allArticleList)
                        }
                    }
                }?.awaitAll()

                ensureActive()
                val shouldShowSource = selectedSource?.isList ?: false

                val mappedArticles = allArticleList.map { article ->
                    ensureActive()
                    ArticleDTO.fromModel(article).apply {
                        guid?.let { guid ->
                            date?.let { date ->
                                read = readArticleDao.getByGuidAndDate(guid, date) != null
                                starred = articlesRepository.getByGuidAndDate(guid, date) != null
                            }
                            showSource = shouldShowSource
                        }
                    }
                }.filter {
                    ensureActive()
                    it.isValid
                }.toMutableList()
                ensureActive()

                allArticles.clear()
                allArticles.addAll(mappedArticles)

                if (prefManager.getArticleFilter() != ArticleFilterType.Starred) {
                    this@GenericArticlesFragmentViewModel.shouldScrollToTop = scrollToTop
                    val result = applyFilters()

                    articles.postValue(result)
                }
                state.postValue(NetworkState.SUCCESS)
                val duration = System.currentTimeMillis() - startTime
                Timber.i("Fetching articles finished in $duration ms")
            } catch (e: IOException) {
                Timber.e(e)
                state.postValue(NetworkState.Companion.error(GenericException()))
            }
        }
    }

    private suspend fun applyFilters(): MutableList<ArticleDTO> {
        val result = applyArticleFilters(allArticles)

        result.sortByDescending {
            it.date
        }

        result.firstOrNull()?.run {
            expandable = false
            expanded = true
        }
        return result
    }


    /**
     * Returns new list containing new instances of items with applied filters.
     *
     * creating copies of objects to ensure diffCallback never compares two same instances
     */
    private fun applyArticleFilters(articles: MutableList<ArticleDTO>): MutableList<ArticleDTO> {
        var result = mutableListOf<ArticleDTO>()
        when (prefManager.getArticleFilter()) {
            ArticleFilterType.Unread -> {
                articles.filter {
                    !it.read
                }.forEach {
                    result.add(it.copy())
                }
            }
            else -> articles.forEach {
                result.add(it.copy())
            }
        }

        result = applySearchQueryFilters(result)

        return result
    }

    private fun applySearchQueryFilters(articles: List<ArticleDTO>): MutableList<ArticleDTO> {
        if (!searchQuery.isNullOrEmpty()) {
            var result = articles.toMutableList()
            searchQuery?.let { query ->
                result = result.filter {
                    it.title?.contains(query, true) ?: true
                            || it.description?.contains(query, true) ?: true
                }.toMutableList()
            }
            return result
        }
        return articles.toMutableList()
    }

    fun markArticleAsRead(article: ArticleDTO) {
        launch(defaultState = null) {
            allArticles.find { it.guid == article.guid }?.read = true
            article.guid?.let { guid ->
                article.date?.let { date ->
                    readArticleDao.insert(
                        ReadArticleEntity(guid = guid, date = date)
                    )
                }
            }
        }
    }


    fun markArticleAsStarred(article: ArticleDTO) {
        launch(defaultState = null) {
            val articleToStar = allArticles.find { it.guid == article.guid }
            val starred = !(articleToStar?.starred ?: true)
            articleToStar?.starred = starred
            article.guid?.let {
                val entity = StarredArticleEntity.fromModel(article)

                if (starred) {
                    starredArticleDao.insert(entity)
                } else {
                    starredArticleDao.delete(entity)
                }
            }
        }
    }


    fun markArticleAsReadOrUnread(article: ArticleDTO) {
        launch(defaultState = null) {
            val articleToMark = allArticles.find { it.guid == article.guid }
            val read = !(articleToMark?.read ?: true)
            articleToMark?.read = read
            article.guid?.let { guid ->
                article.date?.let { date ->
                    val entity = ReadArticleEntity(guid, date)
                    if (read) {
                        readArticleDao.insert(entity)
                    } else {
                        readArticleDao.delete(entity)
                    }
                }
            }
        }
    }

    fun filterArticles(filterType: ArticleFilterType) {
        prefManager.setArticleFilter(filterType)
        if (filterType == ArticleFilterType.Starred) {
            launch(defaultState = null) {
                loadStarredArticles()
            }
        } else {
            if (allArticles.isNotEmpty()) {//do nothing if currently loading (filters will be applied when loading is finished)
                launch(defaultState = null) {
                    val result = applyFilters()
                    articles.postValue(result)
                }
            }
        }
    }


    fun filterBySearchQuery(query: String?) {
        this.searchQuery = query
        filterArticles(prefManager.getArticleFilter())
    }

    private suspend fun loadStarredArticles() {
        Timber.i("loading starred articles")
        val selectedSource = getSource()
        val fromDB: MutableList<StarredArticleEntity> = mutableListOf()

        selectedSource?.let {
            for (url in it.URLs) {
                fromDB.addAll(articlesRepository.getBySourceUrl(url))
            }
        }

        val mapped = fromDB.map { starredEntity ->
            ArticleDTO.fromDb(starredEntity).apply {
                guid?.let { guid ->
                    date?.let { date ->
                        read = readArticleDao.getByGuidAndDate(guid, date) != null
                    }
                    showSource = selectedSource == null
                }
            }
        }
        articles.postValue(applySearchQueryFilters(mapped))
    }

    abstract suspend fun getSource(): RSSSourceDTO?

}

