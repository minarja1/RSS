package cz.minarik.nasapp.ui.news

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.model.RSSSourceDTO
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.UniversePrefManager
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class ArticlesFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val prefManager: UniversePrefManager,
    val sourceRepository: RSSSourceRepository,
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    companion object {
        const val cacheExpirationMillis = 1000L * 60L * 60L // 1 hour
    }

    val articles: MutableLiveData<List<ArticleDTO>> = MutableLiveData()
    private val allSources: MutableList<RSSSourceDTO> = mutableListOf()

    init {
        sourceRepository.updateRSSSourcesFromRealtimeDB()
        updateSourcesAndReload()
    }

    fun updateSourcesAndReload() {
        defaultScope.launch {
            allSources.clear()
            allSources.addAll(sourceDao.getAll().map {
                RSSSourceDTO.fromEntity(it)
            })
            loadNews()
        }
    }

    fun forceReload() {
        state.postValue(NetworkState.LOADING)
        loadNews()
    }

    private fun loadNews() {
        defaultScope.launch {
            state.postValue(NetworkState.LOADING)

            val allArticles = mutableListOf<Article>()

            val parser = Parser.Builder()
                .context(context)
                .charset(Charset.forName("UTF-8"))
                .cacheExpirationMillis(cacheExpirationMillis)
                .build()

            val selectedSource = sourceDao.getSelected()

            //todo something more sophisticcated like lists etc.
            //user selected article source
            if (selectedSource != null) {
                val channel = parser.getChannel(selectedSource.url)
                allArticles.addAll(channel.articles)
            }  //load all sources
            else for (feed in allSources) {
                feed.url?.let {
                    try {
                        val channel = parser.getChannel(it)
                        allArticles.addAll(channel.articles)
                    } catch (e: Exception) {
                        //todo timber and handle exception
                    }
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

    fun getSources(): List<RSSSourceDTO> {
        val allSelection = mutableListOf<RSSSourceDTO>()
        val selectedSources = allSources.filter { it.selected }

        allSelection.addAll(allSources.filter { !it.selected })

        //"all articles" on top
        allSelection.add(
            0, RSSSourceDTO(
                context.getString(R.string.all_articles),
                null,
                imageUrl = null,
                selectedSources.isEmpty()
            )
        )

        //selected item as second
        allSelection.addAll(
            1, selectedSources
        )

        return allSelection
    }

    fun onSourceSelected(sourceSelectionDTO: RSSSourceDTO) {
        launch {
            sourceRepository.setSelected(sourceSelectionDTO.url)
            updateSourcesAndReload()
        }
    }

}