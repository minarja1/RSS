package cz.minarik.nasapp.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {

    private val dataStore: DataStore<Preferences> by lazy {
        RSSApp.applicationContext.createDataStore(name = RSSApp.sharedInstance.dataStoreName)
    }

    private const val SHOULD_SHOW_LP_HINT = "SHOULD_SHOW_LP_HINT"
    private const val ARTICLE_FILTER = "ARTICLE_FILTER"
    private const val INITIAL_SYNC_FINISHED = "INITIAL_SYNC_FINISHED"
    private const val INITIAL_ARTICLE_LOAD_FINISHED = "INITIAL_ARTICLE_LOAD_FINISHED"
    private const val NEW_ARTICLES_FOUND_IDS = "NEW_ARTICLES_FOUND_IDS"

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

    fun getInitialArticleLoadFinished(): Flow<Boolean> {
        return dataStore.getBooleanData(INITIAL_ARTICLE_LOAD_FINISHED)
    }

    suspend fun setInitialArticleLoadFinished(data: Boolean) {
        dataStore.setBooleanData(INITIAL_ARTICLE_LOAD_FINISHED, data)
    }

    fun getArticleFilter(): Flow<ArticleFilterType> {
        return dataStore.getStringData(ARTICLE_FILTER).map {
            ArticleFilterType.fromKey(it) ?: ArticleFilterType.All
        }
    }

    suspend fun setArticleFilter(filter: ArticleFilterType) {
        dataStore.setStringData(ARTICLE_FILTER, filter.key)
    }


    suspend fun setNewArticlesIDs(newArticles: Set<String>) {
        dataStore.setStringSetData(NEW_ARTICLES_FOUND_IDS, newArticles)
    }

    fun getNewArticlesIDs(): Flow<Set<String>> {
        return dataStore.getStringSetData(NEW_ARTICLES_FOUND_IDS)
    }
}

