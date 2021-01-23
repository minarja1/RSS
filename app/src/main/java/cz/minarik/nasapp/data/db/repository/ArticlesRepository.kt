package cz.minarik.nasapp.data.db.repository

import cz.minarik.base.di.base.BaseRepository
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import java.util.*

class ArticlesRepository(
    private val dao: ArticleDao,
    private val sourceDao: RSSSourceDao,
) : BaseRepository() {

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

}