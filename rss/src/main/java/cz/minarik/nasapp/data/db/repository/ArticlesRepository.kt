package cz.minarik.nasapp.data.db.repository

import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Parser
import cz.minarik.base.di.base.BaseRepository
import cz.minarik.base.di.createOkHttpClient
import cz.minarik.nasapp.BuildConfig
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.base.Loading
import cz.minarik.nasapp.base.Success
import cz.minarik.nasapp.base.ViewModelState
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.domain.Article
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.Constants
import cz.minarik.nasapp.utils.addDays
import cz.minarik.nasapp.utils.createCall
import cz.minarik.nasapp.utils.toSyncFeed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*


class ArticlesRepository(
    private val dao: ArticleDao,
    private val sourceDao: RSSSourceDao,
) : BaseRepository() {

    //for debug purposes
    private val fakeNewArticle = false

    /**
     * Number of new articles fetched from server and previously not present in DB.
     */
    val newArticlesCount = MutableLiveData(0)

    val state = MutableLiveData<ViewModelState>()

    private val okHttpClient by lazy {
        createOkHttpClient()
    }

    private val parser by lazy {
        Parser.Builder()
            .context(RSSApp.sharedInstance.applicationContext)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(Constants.articlesCacheExpiration)
            .build()
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
            val maxDate =
                Date().addDays(DataStoreManager.getDbCleanupSettingsItem().first().daysValue)
            source?.let { source ->
                if (source.isAtom) {
                    okHttpClient.createCall(source.url).execute().use { response ->
                        val articles = response.toSyncFeed()?.entries?.asSequence()
                        articles?.let {
                            synchronized(this) {
                                result.addAll(articles.map {
                                    Article.fromLibrary(it, source.url, source.title)
                                }.filter { it.isValid(maxDate) }
                                )
                            }
                        }
                    }
                } else {
                    val articles = parser.getChannel(source.url).articles
                    articles.forEach {
                        it.sourceUrl = source.url
                        it.sourceName = source.title
                    }
                    synchronized(this) {
                        result.addAll(articles.map {
                            Article.fromLibrary(it)
                        }.filter { it.isValid(maxDate) }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    /**
     * Loads articles from server and updates DB
     *
     * [selectedSource] the source for which articles will be updated
     * [notifyNewArticles] whether to notify that new posts have been found
     * [onFinished] callback to when update is finished
     */
    suspend fun updateArticles(
        selectedSource: RSSSource?,
        notifyNewArticles: Boolean = false,
        coroutineScope: CoroutineScope,
        onFinished: (() -> Unit)? = null,
    ) {
        coroutineScope.launch {
            state.postValue(Loading)

            val currentNewest = dao.getNewest().firstOrNull()?.date

            val startTime = System.currentTimeMillis()

            val allArticleList = mutableListOf<Article>()

            (selectedSource?.URLs?.indices)?.map {
                async(Dispatchers.IO) {
                    loadArticlesFromUrl(selectedSource.URLs[it], allArticleList)
                }
            }?.awaitAll()

            val newArticlesFound = mutableListOf<String>()

            if (fakeNewArticle && BuildConfig.DEBUG) {
                for (i in 0 until counter) {
                    allArticleList.add(
                        Article(
                            title = "fake",
                            description = "this is more fake than fakeFile.txt",
                            publicationDate = Date(),
                            guid = System.currentTimeMillis().toString(),
                            sourceUrl = sourceDao.getALl().first().firstOrNull()?.url
                        )
                    )
                }
                counter += 2
            }

            for (article in allArticleList) {
                article.guid?.let { guid ->
                    article.formattedDate?.let { pubDate ->
                        if (!dao.existsByGuidAndDate(guid, pubDate)) {
                            val newEntity = ArticleEntity.fromModel(article)
                            dao.insert(newEntity)

                            currentNewest?.let {
                                if (newEntity.date.after(currentNewest)) newArticlesFound.add(
                                    newEntity.guid
                                )
                            }
                        }
                    }
                }
            }
            val duration = System.currentTimeMillis() - startTime
            onFinished?.invoke()
            state.postValue(Success)

            Timber.i("Repository: fetching articles finished in $duration ms")

            if (notifyNewArticles) notifyNewArticles(newArticlesFound)
        }
    }

    private suspend fun notifyNewArticles(newArticles: List<String>) {
        val initialArticleLoadFinished = DataStoreManager.getInitialArticleLoadFinished().first()
        if (initialArticleLoadFinished) {
            newArticlesCount.postValue(newArticles.count())
        } else {
            DataStoreManager.setInitialArticleLoadFinished(true)
        }
    }


    private suspend fun getAllForSourceConcurrently(source: RSSSource): MutableList<ArticleEntity?> {
        val fromDB: MutableList<ArticleEntity?> = mutableListOf()

        coroutineScope { // limits the scope of concurrency
            (source.URLs).map { url ->
                async(Dispatchers.Default) { // async means "concurrently", context goes here
                    fromDB.addAll(dao.getBySourceUrl(url))
                }
            }.awaitAll() // waits all of them
        } // if any task crashes -- this scope ends with exception

        return fromDB
    }

    var counter = 2
    suspend fun loadFromDB(selectedSource: RSSSource?): List<ArticleDTO> {
        val startTime = System.currentTimeMillis()

        selectedSource?.let {
            val fromDB = getAllForSourceConcurrently(selectedSource)

            val duration = System.currentTimeMillis() - startTime
            Timber.i("${javaClass.name}timer fetching ${fromDB.size} articles in $duration ms")

            val mappingStartTime = System.currentTimeMillis()

            val result = fromDB.filterNotNull().map { entity ->
                ArticleDTO.fromDb(entity).apply {
                    showSource = selectedSource.isList
                }
            }

            val mappingDuration = System.currentTimeMillis() - mappingStartTime
            Timber.i("${javaClass.name}timer mapping ${fromDB.size} articles in $mappingDuration ms")

            return result
        }

        return emptyList()
    }

    fun resetNewArticles() {
        newArticlesCount.postValue(0)
    }

    suspend fun dbCleanUp(date: Date) {
        dao.deleteNonStarredOlderThan(date)
    }

}