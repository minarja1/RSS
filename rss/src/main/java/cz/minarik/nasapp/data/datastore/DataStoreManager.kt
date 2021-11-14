package cz.minarik.nasapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.JsonAdapter
import cz.minarik.base.common.extensions.*
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleFilterType
import cz.minarik.nasapp.data.domain.DbCleanupItem
import cz.minarik.nasapp.utils.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {

    private val context: Context by lazy {
        RSSApp.applicationContext
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(RSSApp.sharedInstance.dataStoreName)

    private const val SHOULD_SHOW_LP_HINT = "SHOULD_SHOW_LP_HINT"
    private const val EXPAND_ALL_CARDS = "EXPAND_ALL_CARDS"
    private const val NOTIFICATION_SETTINGS = "NOTIFICATION_SETTINGS"
    private const val OPEN_ARTICLES_BROWSER = "OPEN_ARTICLES_BROWSER"
    private const val EXTERNAL_BROWSER = "EXTERNAL_BROWSER"
    private const val ARTICLE_FILTER = "ARTICLE_FILTER"
    private const val INITIAL_SYNC_FINISHED = "INITIAL_SYNC_FINISHED"
    private const val INITIAL_ARTICLE_LOAD_FINISHED = "INITIAL_ARTICLE_LOAD_FINISHED"
    private const val NEW_ARTICLES_FOUND_IDS = "NEW_ARTICLES_FOUND_IDS"
    private const val ARTICLE_PUB_DATE_LIMIT = "ARTICLE_PUB_DATE_LIMIT"

    private val notificationSettingsAdapter: JsonAdapter<NotificationSettings> =
        moshi().adapter(NotificationSettings::class.java)

    fun getUseExternalBrowser(): Flow<Boolean> {
        return context.dataStore.getBooleanData(EXTERNAL_BROWSER, false)
    }

    suspend fun setUseExternalBrowser(data: Boolean) {
        context.dataStore.setBooleanData(EXTERNAL_BROWSER, data)
    }

    fun getOpenArticlesInBrowser(): Flow<Boolean> {
        return context.dataStore.getBooleanData(OPEN_ARTICLES_BROWSER, false)
    }

    suspend fun setOpenArticlesInBrowser(data: Boolean) {
        context.dataStore.setBooleanData(OPEN_ARTICLES_BROWSER, data)
    }

    fun getExpandAllCards(): Flow<Boolean> {
        return context.dataStore.getBooleanData(EXPAND_ALL_CARDS, false)
    }

    suspend fun setExpandAllCards(data: Boolean) {
        context.dataStore.setBooleanData(EXPAND_ALL_CARDS, data)
    }

    fun getNotificationSettings(): Flow<NotificationSettings> {
        return context.dataStore.getStringData(NOTIFICATION_SETTINGS).map {
            if(it.isEmpty()){
                NotificationSettings()
            } else {
                notificationSettingsAdapter.fromJson(it) ?: NotificationSettings()
            }
        }
    }

    suspend fun setNotificationSettings(data: NotificationSettings) {
        context.dataStore.setStringData(
            NOTIFICATION_SETTINGS,
            notificationSettingsAdapter.toJson(data)
        )
    }

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

    fun getDbCleanupSettingsItem(): Flow<DbCleanupItem> {
        return context.dataStore.getIntData(ARTICLE_PUB_DATE_LIMIT).map {
            DbCleanupItem.fromDaysValue(it)
        }
    }

    suspend fun setDbCleanupSettingsItem(data: DbCleanupItem) {
        context.dataStore.setIntData(ARTICLE_PUB_DATE_LIMIT, data.daysValue)
    }

    fun getArticleFilter(): Flow<ArticleFilterType> {
        return context.dataStore.getStringData(ARTICLE_FILTER).map {
            ArticleFilterType.fromKey(it) ?: ArticleFilterType.All
        }
    }

    suspend fun setArticleFilter(filter: ArticleFilterType) {
        context.dataStore.setStringData(ARTICLE_FILTER, filter.key)
    }

}

