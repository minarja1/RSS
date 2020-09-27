package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReadArticleEntity(
    @PrimaryKey
    val guid: String,
)