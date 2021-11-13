package cz.minarik.nasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import cz.minarik.base.data.db.dao.BaseDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RSSSourceDao : BaseDao<RSSSourceEntity> {

    @Query("Select * From RSSSourceEntity where isHidden = 0 order by title")
    suspend fun getAllUnblocked(): List<RSSSourceEntity>

    @Query("Select * From RSSSourceEntity where isHidden = 0 order by title")
    fun getAllUnblockedFlow(): Flow<List<RSSSourceEntity>>

    @Query("Select * From RSSSourceEntity order by title")
    fun getALl(): Flow<List<RSSSourceEntity>>

    @Query("Select * From RSSSourceEntity where isUserAdded = 0 order by title")
    suspend fun getNonUserAdded(): List<RSSSourceEntity>

    @Query("Select * From RSSSourceEntity Where url = :url")
    suspend fun getByUrl(url: String): RSSSourceEntity?

    @Query("Select * From RSSSourceEntity Where isSelected == 1")
    suspend fun getSelected(): RSSSourceEntity?

    @Query("Select * From RSSSourceEntity Where isSelected == 1")
    fun getSelectedFlow(): Flow<RSSSourceEntity?>
}