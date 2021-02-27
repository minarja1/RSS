package cz.minarik.nasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import cz.minarik.base.data.db.dao.BaseDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import java.util.*

@Dao
interface ArticleDao : BaseDao<ArticleEntity> {

    @Query("Select * From ArticleEntity order by date desc")
    suspend fun getAll(): List<ArticleEntity>

    @Query("Select * From ArticleEntity order by date desc limit 1")
    suspend fun getNewest(): List<ArticleEntity>

    @Query("Select * From ArticleEntity Where guid = :guid and date = :date order by date desc")
    suspend fun getByGuidAndDate(guid: String, date: Date): ArticleEntity?

    @Query("SELECT EXISTS(Select * From ArticleEntity Where guid = :guid and date = :date)")
    fun existsByGuidAndDate(guid: String, date: Date): Boolean

    @Query("Select * From ArticleEntity Where sourceUrl = :sourceUrl order by date desc")
    suspend fun getBySourceUrl(sourceUrl: String): List<ArticleEntity>
}