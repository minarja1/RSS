package cz.minarik.nasapp.ui.settings.notifications

import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.domain.NotificationKeyword
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.NotificationSettings
import kotlinx.coroutines.flow.*

class NotificationSettingsViewModel(
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    private val notificationSettings = DataStoreManager.getNotificationSettings()

    // todo only with notifications on
    val sources: StateFlow<List<RSSSource>> = sourceDao.getALl().map { allSources ->
        allSources.map {
            RSSSource.fromEntity(it)
        }
    }.stateIn(scope = ioScope, initialValue = listOf(), started = SharingStarted.Lazily)

    val viewState = combine(
        notificationSettings,
        sources,
    ) { notifications, sources ->
        NotificationSettingsState(
            notificationSettings = notifications,
            sources = sources,
        )
    }.stateIn(
        scope = ioScope,
        initialValue = NotificationSettingsState(
            notificationSettings = NotificationSettings(),
            sources = listOf(),
        ),
        started = SharingStarted.Lazily
    )

    fun setNotifyAll(checked: Boolean) {
        launch {
            saveNotificationSettings(
                viewState.value.notificationSettings.copy(
                    notifyAll = checked,
                )
            )
        }
    }


    private suspend fun saveNotificationSettings(notificationSettings: NotificationSettings) {
        DataStoreManager.setNotificationSettings(notificationSettings)
    }

    fun addNotificationKeyword(keyword: NotificationKeyword) {
        launch {
            val viewState = viewState.value
            val mutable =
                viewState.notificationSettings.keyWords.toMutableList()
            mutable.add(0, keyword)

            saveNotificationSettings(
                viewState.notificationSettings.copy(
                    keyWords = mutable.toList()
                )
            )
        }
    }

    fun removeNotificationKeyword(keyword: NotificationKeyword) {
        launch {
            val viewState = viewState.value
            val mutable =
                viewState.notificationSettings.keyWords.toMutableList()
            mutable.remove(keyword)

            saveNotificationSettings(
                viewState.notificationSettings.copy(
                    keyWords = mutable.toList()
                )
            )
        }
    }

}