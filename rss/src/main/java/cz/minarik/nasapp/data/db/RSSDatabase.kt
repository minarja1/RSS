package cz.minarik.nasapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import cz.minarik.base.common.extensions.moshi
import cz.minarik.nasapp.data.db.dao.ArticleDao
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceListEntity
import java.util.*

@Database(
    entities = [
        RSSSourceEntity::class,
        RSSSourceListEntity::class,
        ArticleEntity::class,
    ],
    version = RSSDatabase.Version
)

@TypeConverters(BaseRoomDatabaseConverters::class)
abstract class RSSDatabase : RoomDatabase() {

    companion object {
        const val Version = 3
        const val Name = "RSSDatabase"
    }

    abstract fun rssSourceDao(): RSSSourceDao
    abstract fun starredArticleDao(): ArticleDao
}

class BaseRoomDatabaseConverters {

    private val stringListAdapter: JsonAdapter<List<String>> =
        moshi().adapter(Types.newParameterizedType(List::class.java, String::class.java))

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        return stringListAdapter.fromJson(json)?.let {
            it
        } ?: listOf()
    }

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return stringListAdapter.toJson(strings)
    }

}