package cz.minarik.nasapp.ui.news

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.ui.custom.ArticleDTO
import java.nio.charset.Charset

class NewsFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
) : BaseViewModel() {

    init {
        loadNews()
    }

    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    //url of RSS feed
    private val url = "https://www.jpl.nasa.gov/multimedia/rss/news.xml"
    private val url2 = "https://www.jpl.nasa.gov/multimedia/podcast/podfeed.xml"

    fun loadNews() {
        launch {
            state.postValue(NetworkState.LOADING)
            val start = System.currentTimeMillis()
            val parser = Parser.Builder()
                .context(context)
                .charset(Charset.forName("ISO-8859-7"))
                .cacheExpirationMillis(24L * 60L * 60L * 100L) // one day
                .build()

            val channel = parser.getChannel(url)
            val channel2 = parser.getChannel(url2)

            val allArticles = mutableListOf<Article>()
            allArticles.addAll(channel.articles)
            allArticles.addAll(channel2.articles)

            val mapped = allArticles.map { article ->
                ArticleDTO.fromApi(article).apply {
                    guid?.let {
                        read = readArticleDao.getByGuid(it) != null
                    }
                }
            }.filter {
                it.isValid
            }.toMutableList()

            mapped.sortByDescending {
                it.date
            }

            articles.postValue(mapped)
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