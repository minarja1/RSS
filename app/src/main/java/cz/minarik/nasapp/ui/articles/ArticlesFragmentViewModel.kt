package cz.minarik.nasapp.ui.articles

import android.content.Context
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.network.RssApiService
import cz.minarik.nasapp.utils.UniversePrefManager

class ArticlesFragmentViewModel(
    private val context: Context,
    readArticleDao: ReadArticleDao,
    articlesRepository: ArticlesRepository,
    articleDao: ArticleDao,
    prefManager: UniversePrefManager,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
    rssApiService: RssApiService,
) : GenericArticlesFragmentViewModel(
    context,
    readArticleDao,
    articlesRepository,
    articleDao,
    prefManager,
    sourceDao,
    sourceListDao,
    rssApiService,
) {
    override suspend fun getSource(): RSSSource? {
        return sourceDao.getSelected()?.let {
            RSSSource.fromEntity(it)
        } ?: sourceListDao.getSelected()?.let {
            RSSSource.fromEntity(it)
        } ?: RSSSourceRepository.createFakeListItem(
            context,
            sourceDao.getAllUnblocked().map { it.url },
            true
        )
    }
}