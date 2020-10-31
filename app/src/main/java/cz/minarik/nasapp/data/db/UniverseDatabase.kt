package cz.minarik.nasapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import cz.minarik.base.common.extensions.moshi
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.dao.StarredArticleDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity
import cz.minarik.nasapp.data.db.entity.StarredArticleEntity
import java.util.*

@Database(
    entities = [
        ReadArticleEntity::class,
        RSSSourceEntity::class,
        StarredArticleEntity::class,
    ],
    version = UniverseDatabase.Version
)

@TypeConverters(BaseRoomDatabaseConverters::class)
abstract class UniverseDatabase : RoomDatabase() {

    companion object {
        const val Version = 1
        const val Name = "UniverseDatabase"
    }

    abstract fun readArticleDao(): ReadArticleDao
    abstract fun rssSourceDao(): RSSSourceDao
    abstract fun starredArticleDao(): StarredArticleDao
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