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
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.UniversePrefManager
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.Charset

class ArticlesFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    companion object {
        const val cacheExpirationMillis = 1000L * 60L * 60L // 1 hour
    }

    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    init {
        loadNews()
    }

    fun loadNews() {
        defaultScope.launch {
            state.postValue(NetworkState.LOADING)

            val allArticles = mutableListOf<Article>()

            val parser = Parser.Builder()
                .context(context)
                .charset(Charset.forName("UTF-8"))
                .cacheExpirationMillis(cacheExpirationMillis)
                .build()

            val selectedSource = sourceDao.getSelected()

            //todo something more sophisticated like lists etc.
            //user selected article source
            if (selectedSource != null) {
                val channel = parser.getChannel(selectedSource.url)
                allArticles.addAll(channel.articles)
            }  //load all sources
            else for (feed in sourceDao.getAll()) {
                try {
                    val channel = parser.getChannel(feed.url)
                    allArticles.addAll(channel.articles)
                } catch (e: Exception) {
                    //todo handle exception
                    Timber.e(e)
                }
            }

            val mappedArticles = allArticles.map { article ->
                ArticleDTO.fromApi(article).apply {
                    guid?.let {
                        read = readArticleDao.getByGuid(it) != null
                    }
                }
            }.filter {
                it.isValid
            }.toMutableList()

            mappedArticles.sortByDescending {
                it.date
            }

            mappedArticles.firstOrNull()?.run {
                expanded = true
                expandable = false
            }

            articles.postValue(mappedArticles)
            state.postValue(NetworkState.SUCCESS)
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
}