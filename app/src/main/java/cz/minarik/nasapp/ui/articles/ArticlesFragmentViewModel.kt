package cz.minarik.nasapp.ui.articles

import android.content.Context
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.utils.UniversePrefManager

class ArticlesFragmentViewModel(
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
    override suspend fun getSource(): RSSSourceEntity? {
        return sourceDao.getSelected()
    }
}