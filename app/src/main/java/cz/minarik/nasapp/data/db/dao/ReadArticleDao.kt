package cz.minarik.nasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import cz.minarik.base.data.db.dao.BaseDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity

@Dao
interface ReadArticleDao : BaseDao<ReadArticleEntity> {

    @Query("Select * From ReadArticleEntity")
    suspend fun getAll(): List<ReadArticleEntity>

    @Query("Select * From ReadArticleEntity Where guid = :guid")
    suspend fun getByGuid(guid: String): ReadArticleEntity?

}