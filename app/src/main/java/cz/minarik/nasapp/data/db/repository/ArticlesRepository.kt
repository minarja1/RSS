package cz.minarik.nasapp.data.db.repository

import cz.minarik.base.di.base.BaseRepository
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.StarredArticleEntity
import java.util.*

class ArticlesRepository(
    private val dao: StarredArticleDao,
    private val sourceDao: RSSSourceDao,
) : BaseRepository() {

    suspend fun getBySourceUrl(sourceUrl: String): List<StarredArticleEntity> {
        val list = dao.getBySourceUrl(sourceUrl)
        list.map { article ->
            updateSourceByDB(article)
        }
        return list
    }

    suspend fun getByGuidAndDate(guid: String, date: Date): StarredArticleEntity? {
        val entity = dao.getByGuidAndDate(guid, date)
        updateSourceByDB(entity)
        return entity
    }

    suspend fun getAll(): List<StarredArticleEntity> {
        val list = dao.getAll()
        list.map { article ->
            updateSourceByDB(article)
        }
        return list
    }

    private suspend fun updateSourceByDB(entity: StarredArticleEntity?) {
        val source = sourceDao.getByUrl(entity?.sourceUrl ?: "")
        entity?.sourceName = source?.title
        entity?.sourceUrl = source?.url
    }

}