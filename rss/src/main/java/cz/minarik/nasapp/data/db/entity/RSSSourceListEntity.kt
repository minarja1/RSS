package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import timber.log.Timber

@Entity
data class RSSSourceListEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var title: String? = null,
    var imageUrl: String? = null,
    var isUserAdded: Boolean = false,
    var isSelected: Boolean = false,

    )
