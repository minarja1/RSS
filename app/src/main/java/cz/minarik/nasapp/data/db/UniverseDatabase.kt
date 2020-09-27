package cz.minarik.nasapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.minarik.nasapp.data.db.dao.ReadArticleDao
import cz.minarik.nasapp.data.db.entity.ReadArticleEntity

@Database(
    entities = [
        ReadArticleEntity::class
    ],
    version = UniverseDatabase.Version
)
abstract class UniverseDatabase : RoomDatabase() {

    companion object {
        const val Version = 1
        const val Name = "UniverseDatabase"
    }

    abstract fun readArticleDao(): ReadArticleDao
}