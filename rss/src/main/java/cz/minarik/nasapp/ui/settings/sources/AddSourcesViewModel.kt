package cz.minarik.nasapp.ui.settings.sources

import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.coroutines.flow.map

class AddSourcesViewModel(
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    val allSources = sourceDao.getALl().map { allSources ->
        allSources.map {
            RSSSource.fromEntity(it)
        }
    }

    fun sourceSelected(source: RSSSource) {
        launch {
            sourceDao.getByUrl(source.URLs.first())?.let {
                it.isNotificationsEnabled = !it.isNotificationsEnabled
                sourceDao.update(it)
            }
        }
    }

}