package cz.minarik.nasapp.ui.articles.simple

import android.content.Context
import androidx.lifecycle.MutableLiveData
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.domain.RSSSourceDTO
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.ui.articles.GenericArticlesFragmentViewModel
import cz.minarik.nasapp.utils.UniversePrefManager

class SimpleArticlesFragmentViewModel(
    var sourceUrl: String,
    context: Context,
    readArticleDao: ReadArticleDao,
    articlesRepository: ArticlesRepository,
    starredArticleDao: StarredArticleDao,
    prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
    rssApiService: RssApiService,
) : GenericArticlesFragmentViewModel(
    context,
    readArticleDao,
    articlesRepository,
    starredArticleDao,
    prefManager,
    sourceDao,
    sourceListDao,
    rssApiService,
) {

    private var selectedSource: RSSSourceEntity? = null
    val selectedSourceName = MutableLiveData<String?>()
    val selectedSourceImage = MutableLiveData<String>()

    init {
        loadSelectedSource()
    }

    private fun loadSelectedSource() {
        launch(defaultState = null) {
            selectedSource = sourceDao.getByUrl(sourceUrl)
            selectedSourceName.postValue(selectedSource?.title)
            selectedSourceImage.postValue(selectedSource?.imageUrl)
        }
    }

    override suspend fun getSource(): RSSSourceDTO? {
        return sourceDao.getByUrl(sourceUrl)?.let {
            RSSSourceDTO.fromEntity(it)
        }
    }

}