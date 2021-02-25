package cz.minarik.nasapp.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
object DataStoreManager {

    private val dataStore: DataStore<Preferences> by lazy{
        RSSApp.applicationContext.createDataStore(name = RSSApp.sharedInstance.dataStoreName)
    }

    private const val SHOULD_SHOW_LP_HINT = "SHOULD_SHOW_LP_HINT"
    private const val ARTICLE_FILTER = "ARTICLE_FILTER"
    private const val INITIAL_SYNC_FINISHED = "INITIAL_SYNC_FINISHED"
    private const val NEW_ARTICLES_FOUND = "NEW_ARTICLES_FOUND"

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

    fun getNewArticlesFound(): Flow<Int> {
        return dataStore.getIntData(NEW_ARTICLES_FOUND)
    }

    suspend fun setNewArticlesFound(data: Int) {
        dataStore.setIntData(NEW_ARTICLES_FOUND, data)
    }

    suspend fun incrementNewArticlesFound(data: Int) {
        dataStore.incrementIntData(NEW_ARTICLES_FOUND, data)
    }
}

