package cz.minarik.nasapp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class RSSSourceListDataEntity(
    @Embedded
    val rssSourceEntity: RSSSourceListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "url"
    )
    val sources: List<RSSSourceEntity>
)
