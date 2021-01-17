package cz.minarik.nasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import cz.minarik.base.data.db.dao.BaseDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceListDataEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceListEntity

@Dao
interface RSSSourceListDao : BaseDao<RSSSourceListEntity> {

    @Query("Select * From RSSSourceListEntity")
    suspend fun getAll(): List<RSSSourceListDataEntity>

    @Query("Select * From RSSSourceListEntity where isUserAdded = 0 order by title")
    suspend fun getNonUserAdded(): List<RSSSourceListDataEntity>

    @Query("Select * From RSSSourceListEntity Where id = :id")
    suspend fun getById(id: String): RSSSourceListDataEntity?


    @Query("Select * From RSSSourceListEntity Where isSelected == 1")
    suspend fun getSelected(): RSSSourceListDataEntity?

}