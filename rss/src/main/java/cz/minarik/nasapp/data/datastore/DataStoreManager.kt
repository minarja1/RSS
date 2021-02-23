package cz.minarik.nasapp.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.utils.getBooleanData
import cz.minarik.nasapp.utils.getStringData
import cz.minarik.nasapp.utils.setBooleanData
import cz.minarik.nasapp.utils.setStringData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {

    private val dataStore: DataStore<Preferences> =
        RSSApp.applicationContext.createDataStore(name = RSSApp.sharedInstance.dataStoreName)

    private const val LONG_PRESS_SOURCE_DISMISSED = "LONG_PRESS_SOURCE_DISMISSED"
    private const val ARTICLE_FILTER = "ARTICLE_FILTER"

    fun getLongPresSourceDismissed(): Flow<Boolean> {
        return dataStore.getBooleanData(LONG_PRESS_SOURCE_DISMISSED)
    }

    suspend fun setLongPressSourceDismissed(data: Boolean) {
        dataStore.setBooleanData(LONG_PRESS_SOURCE_DISMISSED, data)
    }

    fun getArticleFilter(): Flow<ArticleFilterType> {
        return dataStore.getStringData(ARTICLE_FILTER).map {
            ArticleFilterType.fromKey(it) ?: ArticleFilterType.All
        }
    }

    suspend fun setArticleFilter(filter: ArticleFilterType) {
        dataStore.setStringData(ARTICLE_FILTER, filter.key)
    }
}