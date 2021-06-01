package cz.minarik.nasapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cz.minarik.base.common.extensions.*
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {

    private val context: Context by lazy {
        RSSApp.applicationContext
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(RSSApp.sharedInstance.dataStoreName)

    private const val SHOULD_SHOW_LP_HINT = "SHOULD_SHOW_LP_HINT"
    private const val ARTICLE_FILTER = "ARTICLE_FILTER"
    private const val INITIAL_SYNC_FINISHED = "INITIAL_SYNC_FINISHED"
    private const val INITIAL_ARTICLE_LOAD_FINISHED = "INITIAL_ARTICLE_LOAD_FINISHED"
    private const val NEW_ARTICLES_FOUND_IDS = "NEW_ARTICLES_FOUND_IDS"
    private const val LAST_SOURCE_UPDATE = "LAST_SOURCE_UPDATE"

    fun getShouldShowLongPressHint(): Flow<Boolean> {
        return context.dataStore.getBooleanData(SHOULD_SHOW_LP_HINT, true)
    }

    suspend fun setShouldShowLongPressHint(data: Boolean) {
        context.dataStore.setBooleanData(SHOULD_SHOW_LP_HINT, data)
    }

    fun getInitialSyncFinished(): Flow<Boolean> {
        return context.dataStore.getBooleanData(INITIAL_SYNC_FINISHED)
    }

    suspend fun setInitialSyncFinished(data: Boolean) {
        context.dataStore.setBooleanData(INITIAL_SYNC_FINISHED, data)
    }

    fun getInitialArticleLoadFinished(): Flow<Boolean> {
        return context.dataStore.getBooleanData(INITIAL_ARTICLE_LOAD_FINISHED)
    }

    suspend fun setInitialArticleLoadFinished(data: Boolean) {
        context.dataStore.setBooleanData(INITIAL_ARTICLE_LOAD_FINISHED, data)
    }

    fun getLastSourcesUpdate(): Flow<Long> {
        return context.dataStore.getLongData(LAST_SOURCE_UPDATE)
    }

    suspend fun setLastSourcesUpdate(data: Long) {
        context.dataStore.setLongData(LAST_SOURCE_UPDATE, data)
    }

    fun getArticleFilter(): Flow<ArticleFilterType> {
        return context.dataStore.getStringData(ARTICLE_FILTER).map {
            ArticleFilterType.fromKey(it) ?: ArticleFilterType.All
        }
    }

    suspend fun setArticleFilter(filter: ArticleFilterType) {
        context.dataStore.setStringData(ARTICLE_FILTER, filter.key)
    }

    suspend fun setNewArticlesIDs(newArticles: Set<String>) {
        context.dataStore.setStringSetData(NEW_ARTICLES_FOUND_IDS, newArticles)
    }

    suspend fun resetNewArticleIDs() {
        context.dataStore.setStringSetData(NEW_ARTICLES_FOUND_IDS, setOf())
    }

    fun getNewArticlesIDs(): Flow<Set<String>> {
        return context.dataStore.getStringSetData(NEW_ARTICLES_FOUND_IDS)
    }

}

