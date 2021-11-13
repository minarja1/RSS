package cz.minarik.nasapp.ui.sources.selection

import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.coroutines.flow.map

class SourcesViewModel(
    val sourceRepository: RSSSourceRepository,
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    val selectedSource = sourceDao.getSelectedFlow()

    val allSources = sourceDao.getALl().map { allSources ->
        allSources.map {
            RSSSource.fromEntity(it)
        }
    }

    fun markAsBlocked(source: RSSSource, blocked: Boolean) {
        launch {
            source.URLs.firstOrNull()?.let {
                val entity = sourceDao.getByUrl(it)
                entity?.let {
                    entity.isHidden = blocked
                    entity.isSelected = false
                    sourceDao.update(entity)
                }
            }
        }
    }
}