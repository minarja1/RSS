package cz.minarik.nasapp.ui.articles

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.isInternetAvailable
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.domain.exception.GenericException
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
    val articlesRepository: ArticlesRepository,
    private val articleDao: ArticleDao,
    val prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
    private val rssApiService: RssApiService,
) : BaseViewModel() {

    //all articles (without active filters)
    private val allArticles: MutableList<ArticleDTO> = mutableListOf()

    //shown articles (may be filtered)
    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()

    var shouldScrollToTop: Boolean = false

    var searchQuery: String? = null

    init {
        loadArticles(false, true)
    }

    fun updateDb() {
        if(context.isInternetAvailable){
            launch {
                articlesRepository.updateArticles(getSource()) {
                    loadArticles()
                }
            }
        }
    }

    fun loadArticles(
        scrollToTop: Boolean = false,
        updateDb: Boolean = false
    ) {
        Timber.i("loading article@")
        state.postValue(NetworkState.LOADING)
        ioScope.launch {
            try {
                val startTime = System.currentTimeMillis()

                ensureActive()

                Timber.i("loading starred articles")

                if (updateDb) updateDb()

                val selectedSource = getSource()

                val fromDB: MutableList<ArticleEntity> = mutableListOf()

                selectedSource?.let {
                    for (url in it.URLs) {
                        fromDB.addAll(articlesRepository.getBySourceUrl(url))
                    }
                }

                val mapped = fromDB.map { entity ->
                    ArticleDTO.fromDb(entity).apply {
                        guid?.let { guid ->
                            showSource = selectedSource?.isList ?: false
                        }
                    }
                }

                allArticles.clear()
                allArticles.addAll(mapped)

                this@GenericArticlesFragmentViewModel.shouldScrollToTop = scrollToTop
                val result = applyFilters()

                articles.postValue(result)
                state.postValue(NetworkState.SUCCESS)
                val duration = System.currentTimeMillis() - startTime
                Timber.i("ViewModel: loading articles finished in $duration ms")
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

    fun markArticleAsRead(article: ArticleDTO) {
        launch(defaultState = null) {
            allArticles.find { it.guid == article.guid }?.read = true
            article.guid?.let { guid ->
                article.date?.let { date ->
                    articleDao.getByGuidAndDate(guid, date)?.run {
                        read = true
                        articleDao.update(this)
                    }
                }
            }
        }
    }


    fun markArticleAsStarred(article: ArticleDTO) {
        launch(defaultState = null) {
            val articleToStar = allArticles.find { it.guid == article.guid }
            val starred = !(articleToStar?.starred ?: true)
            articleToStar?.starred = starred
            article.guid?.let { guid ->
                article.date?.let { date ->
                    articleDao.getByGuidAndDate(guid, date)?.run {
                        this.starred = starred
                        articleDao.update(this)
                    }
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
                    articleDao.getByGuidAndDate(guid, date)?.run {
                        this.read = read
                        articleDao.update(this)
                    }
                }
            }
        }
    }

    fun filterArticles(filterType: ArticleFilterType) {
        prefManager.setArticleFilter(filterType)

        if (allArticles.isNotEmpty()) {//do nothing if currently loading (filters will be applied when loading is finished)
            launch(defaultState = null) {
                val result = applyFilters()
                articles.postValue(result)
            }
        }
    }


    fun filterBySearchQuery(query: String?) {
        this.searchQuery = query
        filterArticles(prefManager.getArticleFilter())
    }

    private suspend fun loadArticlesFromDB() {
        Timber.i("loading starred articles")
        val selectedSource = getSource()
        val fromDB: MutableList<ArticleEntity> = mutableListOf()

        selectedSource?.let {
            for (url in it.URLs) {
                fromDB.addAll(articlesRepository.getBySourceUrl(url))
            }
        }

        val mapped = fromDB.map { entity ->
            ArticleDTO.fromDb(entity).apply {
                guid?.let { guid ->
                    showSource = selectedSource?.isList ?: false
                }
            }
        }
        articles.postValue(applySearchQueryFilters(mapped))
    }

    abstract suspend fun getSource(): RSSSource?

}

