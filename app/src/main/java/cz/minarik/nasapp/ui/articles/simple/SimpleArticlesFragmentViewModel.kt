package cz.minarik.nasapp.ui.articles.simple

import android.content.Context
import androidx.lifecycle.MutableLiveData
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
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
) : GenericArticlesFragmentViewModel(
    context,
    readArticleDao,
    articlesRepository,
    starredArticleDao,
    prefManager,
    sourceDao
) {

    private var selectedSource: RSSSourceEntity? = null
    val selectedSourceName = MutableLiveData<String?>()
    val selectedSourceImage = MutableLiveData<String>()

    init {
        launch {
            loadSelectedSource()
        }
    }

    private fun loadSelectedSource(){
        launch {
            selectedSource = sourceDao.getByUrl(sourceUrl)
            selectedSourceName.postValue(selectedSource?.title)
            selectedSourceImage.postValue(selectedSource?.imageUrl)
        }
    }

    override suspend fun getSource(): RSSSourceEntity? {
        return sourceDao.getByUrl(sourceUrl)
    }

}