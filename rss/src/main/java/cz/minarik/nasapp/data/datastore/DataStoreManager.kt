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

    private const val SHOULD_SHOW_LP_HINT = "SHOULD_SHOW_LP_HINT"
    private const val ARTICLE_FILTER = "ARTICLE_FILTER"
    private const val INITIAL_SYNC_FINISHED = "INITIAL_SYNC_FINISHED"

    fun getShouldShowLongPressHint(): Flow<Boolean> {
        return dataStore.getBooleanData(SHOULD_SHOW_LP_HINT, true)
    }

    suspend fun setShouldShowLongPressHint(data: Boolean) {
        dataStore.setBooleanData(SHOULD_SHOW_LP_HINT, data)
    }

    fun getInitialSyncFinished(): Flow<Boolean> {
        return dataStore.getBooleanData(INITIAL_SYNC_FINISHED)
    }

    suspend fun setInitialSyncFinished(data: Boolean) {
        dataStore.setBooleanData(INITIAL_SYNC_FINISHED, data)
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