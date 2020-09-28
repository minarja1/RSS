package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RSSSourceEntity(
    @PrimaryKey
    var url: String,

    var title: String? = null,
    var imageUrl: String? = null,
    var isUserAdded: Boolean = false,
    var isSelected: Boolean = false,

    )