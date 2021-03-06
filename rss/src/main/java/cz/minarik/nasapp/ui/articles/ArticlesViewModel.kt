package cz.minarik.nasapp.ui.articles

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import cz.minarik.base.common.extensions.isInternetAvailable
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.domain.exception.GenericException
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.RSSPrefManager
import kotlinx.coroutines.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException

class ArticlesViewModel(
    private val context: Context,
    val articlesRepository: ArticlesRepository,
    private val articleDao: ArticleDao,
    val prefManager: RSSPrefManager,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
    private val rssApiService: RssApiService,
) : BaseViewModel() {

    //all articles (without active filters)
    private val allArticles: MutableList<ArticleDTO> = mutableListOf()

    //shown articles (may be filtered)
    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    //all articles (without active filters)
    private val allArticlesSimple: MutableList<ArticleDTO> = mutableListOf()

    //shown articles (may be filtered)
    val articlesSimple: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    val state: MutableLiveData<NetworkState> = MutableLiveData<NetworkState>()

    var isInSimpleMode = false

    var shouldScrollToTop: Boolean = false

    var searchQuery: String? = null

    var isFromSwipeRefresh: Boolean = false

    init {
        loadArticles(scrollToTop = false, updateDb = true)
    }

    private fun updateDb() {
        if (context.isInternetAvailable) {
            launch {
                articlesRepository.updateArticles(getSource()) {
                    loadArticles()
                }
            }
        }
    }

    fun loadArticles(
        scrollToTop: Boolean = false,
        updateDb: Boolean = false,
        isFromSwipeRefresh: Boolean = false,
    ) {
        this.isFromSwipeRefresh = isFromSwipeRefresh
        Timber.i("loading article@")
        state.postValue(NetworkState.LOADING)
        ioScope.launch {
            try {
                val startTime = System.currentTimeMillis()

                ensureActive()

                Timber.i("loading starred articles")

                val selectedSource = if (isInSimpleMode) getSourceSimple() else getSource()

                val fromDB: MutableList<ArticleEntity> = mutableListOf()

                selectedSource?.let {
                    for (url in it.URLs) {
                        fromDB.addAll(articleDao.getBySourceUrl(url))
                    }
                }

                val mapped = fromDB.map { entity ->
                    ArticleDTO.fromDb(entity).apply {
                        guid?.let { guid ->
                            showSource = selectedSource?.isList ?: false
                            this.sourceUrl?.let {
                                openExternally =
                                    sourceDao.getByUrl(it)?.forceOpenExternally ?: false
                            }
                        }
                    }
                }

                if (isInSimpleMode) {
                    allArticlesSimple.clear()
                    allArticlesSimple.addAll(mapped)
                } else {
                    allArticles.clear()
                    allArticles.addAll(mapped)
                }

                this@ArticlesViewModel.shouldScrollToTop = scrollToTop
                val result = applyFilters()

                if (isInSimpleMode) {
                    articlesSimple.postValue(result)
                } else {
                    articles.postValue(result)
                }
                state.postValue(NetworkState.SUCCESS)
                val duration = System.currentTimeMillis() - startTime

                if (updateDb) updateDb()
                Timber.i("ViewModel: loading articles finished in $duration ms")
                this@ArticlesViewModel.isFromSwipeRefresh = false
            } catch (e: IOException) {
                Timber.e(e)
                state.postValue(NetworkState.Companion.error(GenericException()))
            }
        }
    }

    private suspend fun applyFilters(): MutableList<ArticleDTO> {
        val result = applyArticleFilters(if (isInSimpleMode) allArticlesSimple else allArticles)

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

    fun markArticleAsStarred(article: ArticleDTO) {
        launch {
            val source = if (isInSimpleMode) allArticlesSimple else allArticles
            val articleToStar = source.find { (it.guid == article.guid && it.date == article.date) }
            val starred = !(articleToStar?.starred ?: true)
            articleToStar?.starred = starred
            article.starred = starred
            article.guid?.let { guid ->
                article.date?.let { date ->
                    articleDao.getByGuidAndDate(guid, date)?.run {
                        this.starred = starred
                        articleDao.update(this)
                    }
                }
            }
            articleStarredLiveData.postValue(true)
            invalidateArticleStates(article)
        }
    }

    private suspend fun invalidateArticleStates(article: ArticleDTO) {
        articlesSimple.value?.firstOrNull {
            it.guid == article.guid && it.date == article.date
        }?.let {
            updateArticleState(it)
        }

        articles.value?.firstOrNull {
            it.guid == article.guid && it.date == article.date
        }?.let {
            updateArticleState(it)
        }
    }

    private suspend fun updateArticleState(article: ArticleDTO) {
        article.guid?.let { guid ->
            article.date?.let { date ->
                val dbArticle = articleDao.getByGuidAndDate(guid, date)
                article.starred = dbArticle?.starred ?: false
                article.read = dbArticle?.read ?: false
            }
        }
    }

    fun markArticleAsReadOrUnread(article: ArticleDTO, forceRead: Boolean = false) {
        launch {
            val source = if (isInSimpleMode) allArticlesSimple else allArticles
            val articleToMark = source.find { it.guid == article.guid && it.date == article.date }
            val read = forceRead || !(articleToMark?.read ?: true)
            articleToMark?.read = read
            article.read = read
            article.guid?.let { guid ->
                article.date?.let { date ->
                    articleDao.getByGuidAndDate(guid, date)?.run {
                        this.read = read
                        articleDao.update(this)
                    }
                }
            }
            invalidateArticleStates(article)
        }
    }

    fun filterArticles(filterType: ArticleFilterType) {
        prefManager.setArticleFilter(filterType)

//        if (allArticles.isNotEmpty()) {
        launch {
            val result = applyFilters()
            if (isInSimpleMode) {
                articlesSimple.postValue(result)
            } else {
                articles.postValue(result)
            }
        }
//        }
    }


    fun filterBySearchQuery(query: String?) {
        this.searchQuery = query
        filterArticles(prefManager.getArticleFilter())
    }

    suspend fun getSource(): RSSSource? {
        return sourceDao.getSelected()?.let {
            RSSSource.fromEntity(it)
        } ?: sourceListDao.getSelected()?.let {
            RSSSource.fromEntity(it)
        } ?: RSSSourceRepository.createFakeListItem(
            context,
            sourceDao.getAllUnblocked().map { it.url },
            true
        )
    }

    //for SIMPLE loading (just a single source)_____________________________________________________
    private var selectedSource: RSSSourceEntity? = null
    val selectedSourceName = MutableLiveData<String?>()
    val selectedSourceImage = MutableLiveData<String>()
    private lateinit var sourceUrl: String

    fun loadSelectedSource(sourceUrl: String) {
        this.sourceUrl = sourceUrl
        loadArticles(scrollToTop = false, updateDb = true)
        launch {
            selectedSource = sourceDao.getByUrl(sourceUrl)
            selectedSourceName.postValue(selectedSource?.title)
            selectedSourceImage.postValue(selectedSource?.imageUrl)
        }
    }


    private suspend fun getSourceSimple(): RSSSource? {
        return sourceDao.getByUrl(sourceUrl)?.let {
            RSSSource.fromEntity(it)
        }
    }

    //ARTICLE DETAIL________________________________________________________________________________
    val articleLiveData: MutableLiveData<Article?> = MutableLiveData()
    val articleStarredLiveData: MutableLiveData<Boolean> = MutableLiveData()
    lateinit var articleDetailDTO: ArticleDTO

    fun loadArticleDetail(article: ArticleDTO) {
        this.articleDetailDTO = article
        launch {
            articleDetailDTO.link?.let { articleUrl ->
                val parse = articleUrl.toHttpUrl()
                val doc = Jsoup.connect(articleUrl).get()
                val article = ArticleExtractor(parse, doc)
                    .extractMetadata()
                    .extractContent()
                    .article

                articleLiveData.postValue(article)
            }
        }
    }
}

