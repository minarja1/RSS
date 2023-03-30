package cz.minarik.nasapp.ui.settings.sources

import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.data.domain.RSSSourceNotificationListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AddSourcesViewModel(
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    val allSources: StateFlow<List<RSSSourceNotificationListItem>> =
        sourceDao.getALl().map { allSources ->
            allSources.map {
                RSSSourceNotificationListItem.fromEntity(it)
            }
        }.stateIn(scope = ioScope, initialValue = listOf(), started = SharingStarted.Lazily)

    fun sourceSelected(source: RSSSourceNotificationListItem) {
        launch {
            sourceDao.getByUrl(source.URLs.first())?.let {
                it.isNotificationsEnabled = !it.isNotificationsEnabled
                sourceDao.update(it)
            }
        }
    }

}