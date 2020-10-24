package cz.minarik.nasapp.ui.articles

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.model.ArticleFilterType
import cz.minarik.nasapp.data.model.exception.NoConnectionException
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.UniversePrefManager
import cz.minarik.nasapp.utils.isInternetAvailable
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.Charset

class ArticlesFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    val prefManager: UniversePrefManager,
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
        val channel = parser.getChannel(url)
        result.addAll(channel.articles)
    }

    fun loadArticles(force: Boolean = false, scrollToTop: Boolean = false) {
        currentArticleLoadingJob?.cancel()
        currentArticleLoadingJob = defaultScope.launch {
            if (!context.isInternetAvailable) {
                state.postValue(NetworkState.Companion.error(NoConnectionException()))
                return@launch
            }
            Timber.i("loading articles, force = $force")
            state.postValue(NetworkState.LOADING)

            val allArticleList = mutableListOf<Article>()

            ensureActive()
            val selectedSource = sourceDao.getSelected()

            //todo something more sophisticated like lists etc.
            //user selected article source
            if (selectedSource != null) {
                loadArticlesFromUrl(selectedSource.url, force, allArticleList)
            }  //load all sources
            else for (feed in sourceDao.getAll()) {
                ensureActive()
                loadArticlesFromUrl(feed.url, force, allArticleList)
            }

            val mappedArticles = allArticleList.map { article ->
                ArticleDTO.fromApi(article).apply {
                    guid?.let {
                        read = readArticleDao.getByGuid(it) != null
                    }
                }
            }.filter {
                it.isValid
            }.toMutableList()
            ensureActive()


            allArticles.clear()
            allArticles.addAll(mappedArticles)
            applyFiltersAndPostResult(scrollToTop)
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
        launch {
            allArticles.find { it.guid == article.guid }?.read = true
            article.guid?.let {
                readArticleDao.insert(
                    ReadArticleEntity(guid = it)
                )
            }
        }
    }


    fun markArticleAsStarred(article: ArticleDTO) {
        launch {
            val articleToStar = allArticles.find { it.guid == article.guid }
            articleToStar?.starred = articleToStar?.starred ?: true
            article.guid?.let {
                //todo save article
            }
        }
    }

    fun filterArticles(filterType: ArticleFilterType) {
        prefManager.setArticleFilter(filterType)
        applyFiltersAndPostResult()
    }

}