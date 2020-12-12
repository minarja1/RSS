package cz.minarik.nasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import cz.minarik.base.data.db.dao.BaseDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.db.entity.StarredArticleEntity
import java.util.*

@Dao
interface StarredArticleDao : BaseDao<StarredArticleEntity> {

    @Query("Select * From StarredArticleEntity order by date desc")
    suspend fun getAll(): List<StarredArticleEntity>

    @Query("Select * From StarredArticleEntity Where guid = :guid and date = :date order by date desc")
    suspend fun getByGuidAndDate(guid: String, date: Date): StarredArticleEntity?

    @Query("Select * From StarredArticleEntity Where sourceUrl = :sourceUrl order by date desc")
    suspend fun getBySourceUrl(sourceUrl: String): List<StarredArticleEntity>
}