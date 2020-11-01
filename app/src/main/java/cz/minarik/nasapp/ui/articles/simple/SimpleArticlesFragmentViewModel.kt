package cz.minarik.nasapp.ui.articles.simple

import android.content.Context
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.ui.articles.GenericArticlesFragmentViewModel
import cz.minarik.nasapp.utils.UniversePrefManager

class SimpleArticlesFragmentViewModel(
    private val context: Context,
    private val readArticleDao: ReadArticleDao,
    private val articlesRepository: ArticlesRepository,
    private val starredArticleDao: StarredArticleDao,
    override val prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
) : GenericArticlesFragmentViewModel(
    context,
    readArticleDao,
    articlesRepository,
    starredArticleDao,
    prefManager,
    sourceDao
) {
    override suspend fun getSource(): RSSSourceEntity? {
        return sourceDao.getSelected()
    }

}