package cz.minarik.nasapp.ui.articles

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.isInternetAvailable
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.db.entity.StarredArticleEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.model.ArticleFilterType
import cz.minarik.nasapp.data.model.exception.GenericException
import cz.minarik.nasapp.data.model.exception.NoConnectionException
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.UniversePrefManager
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.charset.Charset

abstract class GenericArticlesFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val articlesRepository: ArticlesRepository,
    private val starredArticleDao: StarredArticleDao,
    open val prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    private val parser
        get() = Parser.Builder()
            .context(context)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(cacheExpirationMillis)
            .build()

    private var currentArticleLoadingJob: Job? = null

    companion object {
        const val cacheExpirationMillis = 1000L * 60L * 60L // 1 hour
    }

    //all articles (without active filters)
    private val allArticles: MutableList<ArticleDTO> = mutableListOf()

    //shown articles (may be filtered)
    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    var shouldScrollToTop: Boolean = false

    init {
        loadArticles(true)
    }

    private suspend fun loadArticlesFromUrl(
        url: String,
        flushCache: Boolean,
        result: MutableList<Article>
    ) {
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
            result.addAll(articles)
        }
    }

    fun loadArticles(
        force: Boolean = false,
        scrollToTop: Boolean = false,
    ) {
        currentArticleLoadingJob?.cancel()
        currentArticleLoadingJob = defaultScope.launch {
            try {
                if (!context.isInternetAvailable) {
                    state.postValue(NetworkState.Companion.error(NoConnectionException()))
                    return@launch
                }
                Timber.i("loading articles, force = $force")
                state.postValue(NetworkState.LOADING)

                val allArticleList = mutableListOf<Article>()

                ensureActive()
                val selectedSource = getSource()

                //get articles from api
                //user selected article source
                if (selectedSource != null) {
                    loadArticlesFromUrl(selectedSource.url, force, allArticleList)
                }  //load all sources
                else {
                    //todo something more sophisticated like lists etc.
                    val allSources = sourceDao.getAll()
                    (allSources.indices).map {
                        ensureActive()
                        async(Dispatchers.IO) {
                            try {
                                loadArticlesFromUrl(allSources[it].url, force, allArticleList)
                            } catch (e: SocketTimeoutException) {
                            }
                        }
                    }.awaitAll()
                }

                ensureActive()
                val shouldShowSource = selectedSource == null

                val mappedArticles = allArticleList.map { article ->
                    ensureActive()
                    ArticleDTO.fromApi(article).apply {
                        guid?.let {
                            read = readArticleDao.getByGuid(it) != null
                            starred = articlesRepository.getByGuid(it) != null
                            showSource = true
                            //todo vratit
//                            showSource = shouldShowSource
                        }
                    }
                }.filter {
                    it.isValid && !it.starred
                }.toMutableList()
                ensureActive()

                //get starred articles from db
                val fromDb = mutableListOf<StarredArticleEntity>()
                if (selectedSource != null) {
                    fromDb.addAll(articlesRepository.getBySourceUrl(selectedSource.url))
                } else {
                    fromDb.addAll(articlesRepository.getAll())
                }
                val mappedFromDb = fromDb.map { starredArticle ->
                    ensureActive()
                    ArticleDTO.fromDb(starredArticle).apply {
                        guid?.let {
                            read = readArticleDao.getByGuid(it) != null
                            showSource = shouldShowSource
                        }
                    }
                }

                allArticles.clear()
                allArticles.addAll(mappedArticles)
                allArticles.addAll(mappedFromDb)
                applyFiltersAndPostResult(scrollToTop)
            } catch (e: IOException) {
                Timber.e(e)
                state.postValue(NetworkState.Companion.error(GenericException()))
            }
        }
    }

    private fun applyFiltersAndPostResult(scrollToTop: Boolean = false) {
        this.shouldScrollToTop = scrollToTop
        val result = applyArticleFilters(allArticles)

        result.sortByDescending {
            it.date
        }

        result.forEachIndexed { index, articleDTO ->
            if (index == 0) {
                articleDTO.expanded = true
                articleDTO.expandable = false
            } else {
                articleDTO.expandable = true
            }
        }

        articles.postValue(result)
        state.postValue(NetworkState.SUCCESS)
    }


    /**
     * Returns new list containing new instances of items with applied filters.
     *
     * creating copies of objects to ensure diffCallback never compares two same instances
     */
    private fun applyArticleFilters(articles: MutableList<ArticleDTO>): MutableList<ArticleDTO> {
        val result = mutableListOf<ArticleDTO>()
        when (prefManager.getArticleFilter()) {
            ArticleFilterType.Unread -> {
                articles.filter {
                    !it.read
                }.forEach {
                    result.add(it.copy())
                }
            }
            ArticleFilterType.Starred -> {
                articles.filter {
                    it.starred
                }.forEach {
                    result.add(it.copy())
                }
            }
            else -> articles.forEach {
                result.add(it.copy())
            }
        }
        return result
    }

    fun markArticleAsRead(article: ArticleDTO) {
        launch(defaultState = null) {
            allArticles.find { it.guid == article.guid }?.read = true
            article.guid?.let {
                readArticleDao.insert(
                    ReadArticleEntity(guid = it)
                )
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

    fun filterArticles(filterType: ArticleFilterType) {
        prefManager.setArticleFilter(filterType)
        applyFiltersAndPostResult()
    }

    abstract suspend fun getSource(): RSSSourceEntity?

}