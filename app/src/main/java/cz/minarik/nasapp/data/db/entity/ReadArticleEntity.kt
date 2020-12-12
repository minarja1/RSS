package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(primaryKeys = ["guid","date"])
data class ReadArticleEntity(
    val guid: String,
    val date: Date
)