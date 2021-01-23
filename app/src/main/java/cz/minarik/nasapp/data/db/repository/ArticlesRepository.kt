package cz.minarik.nasapp.data.db.repository

import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.toDateFromRSS
import cz.minarik.base.di.base.BaseRepository
import cz.minarik.nasapp.UniverseApp
import cz.minarik.nasapp.base.Loading
import cz.minarik.nasapp.base.Success
import cz.minarik.nasapp.base.ViewModelState
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.domain.Article
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.Constants
import kotlinx.coroutines.*
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*

class ArticlesRepository(
    private val dao: ArticleDao,
    private val sourceDao: RSSSourceDao,
) : BaseRepository() {

    val state = MutableLiveData<ViewModelState>()

    private val parser by lazy {
        Parser.Builder()
            .context(UniverseApp.sharedInstance.applicationContext)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(Constants.articlesCacheExpiration)
            .build()
    }

    suspend fun getBySourceUrl(sourceUrl: String): List<ArticleEntity> {
        val list = dao.getBySourceUrl(sourceUrl)
        list.map { article ->
            updateSourceByDB(article)
        }
        return list
    }

    suspend fun getByGuidAndDate(guid: String, date: Date): ArticleEntity? {
        val entity = dao.getByGuidAndDate(guid, date)
        updateSourceByDB(entity)
        return entity
    }

    suspend fun getAll(): List<ArticleEntity> {
        val list = dao.getAll()
        list.map { article ->
            updateSourceByDB(article)
        }
        return list
    }

    private suspend fun updateSourceByDB(entity: ArticleEntity?) {
        val source = sourceDao.getByUrl(entity?.sourceUrl ?: "")
        entity?.sourceName = source?.title
        entity?.sourceUrl = source?.url
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
        result: MutableList<Article>
    ) {
        try {
            Timber.i("Loading articles for url: $url")
            Timber.i("flushing cache for url: $url")
            parser.flushCache(url)
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


    fun updateArticles(selectedSource: RSSSource?, onFinished: (() -> Unit)? = null) {
        GlobalScope.launch {
            state.postValue(Loading)
            val startTime = System.currentTimeMillis()

            val allArticleList = mutableListOf<Article>()

            (selectedSource?.URLs?.indices)?.map {
                async(Dispatchers.IO) {
                    loadArticlesFromUrl(selectedSource.URLs[it], allArticleList)
                }
            }?.awaitAll()

            for (article in allArticleList) {
                article.guid?.let { guid ->
                    article.pubDate?.let { pubDate ->
                        if (!dao.existsByGuidAndDate(guid, pubDate.toDateFromRSS() ?: Date())) {
                            val newEntity = ArticleEntity.fromModel(article)
                            dao.insert(newEntity)
                        }
                    }
                }
            }

            val duration = System.currentTimeMillis() - startTime
            onFinished?.invoke()
            state.postValue(Success)

            Timber.i("Repository: fetching articles finished in $duration ms")
        }
    }

}