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

    companion object {
        const val cacheExpirationMillis = 1000L * 60L * 60L // 1 hour
    }

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
        defaultScope.launch {
            if (!context.isInternetAvailable) {
                state.postValue(NetworkState.Companion.error(NoConnectionException()))
                return@launch
            }
            Timber.i("loading articles, force = $force")
            state.postValue(NetworkState.LOADING)

            val allArticles = mutableListOf<Article>()

            val selectedSource = sourceDao.getSelected()

            //todo something more sophisticated like lists etc.
            //user selected article source
            if (selectedSource != null) {
                loadArticlesFromUrl(selectedSource.url, force, allArticles)
            }  //load all sources
            else for (feed in sourceDao.getAll()) {
                loadArticlesFromUrl(feed.url, force, allArticles)
            }

            var mappedArticles = allArticles.map { article ->
                ArticleDTO.fromApi(article).apply {
                    guid?.let {
                        read = readArticleDao.getByGuid(it) != null
                    }
                }
            }.filter {
                it.isValid
            }.toMutableList()

            mappedArticles = applyArticleFilters(mappedArticles)

            mappedArticles.sortByDescending {
                it.date
            }

            mappedArticles.firstOrNull()?.run {
                expanded = true
                expandable = false
            }

            shouldScrollToTop = scrollToTop

            articles.postValue(mappedArticles)
            state.postValue(NetworkState.SUCCESS)
        }

    }

    private fun applyArticleFilters(articles: MutableList<ArticleDTO>): MutableList<ArticleDTO> {
        return when (prefManager.getArticleFilter()) {
            ArticleFilterType.Unread -> {
                articles.filter {
                    !it.read
                }.toMutableList()
            }
            ArticleFilterType.Starred -> {
                articles.filter {
                    it.starred
                }.toMutableList()
            }
            else -> articles
        }
    }

    fun markArticleAsRead(article: ArticleDTO) {
        launch {
            article.guid?.let {
                readArticleDao.insert(
                    ReadArticleEntity(guid = it)
                )
            }
        }
    }

    fun filterArticles(filterType: ArticleFilterType) {
        prefManager.setArticleFilter(filterType)
        loadArticles()
    }

}