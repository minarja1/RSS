package cz.minarik.nasapp.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.utils.getBooleanData
import cz.minarik.nasapp.utils.setBooleanData
import kotlinx.coroutines.flow.Flow

object DataStoreManager {

    private val dataStore: DataStore<Preferences> =
        RSSApp.applicationContext.createDataStore(name = RSSApp.sharedInstance.dataStoreName)

    private const val LONG_PRESS_SOURCE_DISMISSED = "LONG_PRESS_SOURCE_DISMISSED"

    fun getLongPresSourceDismissed(): Flow<Boolean> {
        return dataStore.getBooleanData(LONG_PRESS_SOURCE_DISMISSED)
    }

    suspend fun setLongPressSourceDismissed(data: Boolean) {
        dataStore.setBooleanData(LONG_PRESS_SOURCE_DISMISSED, data)
    }
}