package cz.minarik.nasapp.ui.sources.selection

import android.content.Context
import androidx.lifecycle.MutableLiveData
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.coroutines.launch

class SourcesViewModel(
    private val context: Context,
    val sourceRepository: RSSSourceRepository,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
) : BaseViewModel() {

    val sourcesSelectionData = MutableLiveData<MutableList<RSSSource>>()
    val sourceSelectionsListsData = MutableLiveData<MutableList<RSSSource>>()

    val sourcesManagementData = MutableLiveData<MutableList<RSSSource>>()
    val sourceManagementListsData = MutableLiveData<MutableList<RSSSource>>()

    val selectedSourceChanged = MutableLiveData<Boolean>()
    val selectedSourceName = MutableLiveData<String?>()
    val selectedSourceImage = MutableLiveData<String>()

    init {
        sourceRepository.updateRSSSourcesFromRealtimeDB()
        updateAll()
    }

    fun updateAll() {
        updateSourcesSelection()
        updateSourcesManagement()
    }

    private fun updateSourcesManagement() {
        defaultScope.launch {
            val allSources: MutableList<RSSSource> = mutableListOf()
            allSources.addAll(sourceDao.getALl().map {
                RSSSource.fromEntity(it)
            })

            val allLists: MutableList<RSSSource> = mutableListOf()
            allLists.addAll(sourceListDao.getAll().map {
                RSSSource.fromEntity(it)
            })

            sourcesManagementData.postValue(allSources)
            sourceManagementListsData.postValue(allLists)
        }
    }


    private fun updateSourcesSelection() {
        defaultScope.launch {
            val allSources: MutableList<RSSSource> = mutableListOf()
            var selectedSourceFound = false
            allSources.addAll(sourceDao.getALl().map {
                RSSSource.fromEntity(it)
            })
            allSources.firstOrNull { it.selected }?.let {
                selectedSourceName.postValue(it.title)
                selectedSourceImage.postValue(it.imageUrl)
                selectedSourceFound = true
            }

            val allLists: MutableList<RSSSource> = mutableListOf()
            allLists.addAll(sourceListDao.getAll().map {
                RSSSource.fromEntity(it)
            })
            allLists.firstOrNull { it.selected }?.let {
                selectedSourceName.postValue(it.title)
                selectedSourceImage.postValue("")
                selectedSourceFound = true
            }

            //"all articles" on top
            //selected when nothing in DB is actually selected
            allLists.add(
                0,
                RSSSourceRepository.createFakeListItem(
                    context,
                    allSources.map { it.URLs[0] },
                    !selectedSourceFound
                )
            )
            if (!selectedSourceFound) {
                selectedSourceName.postValue(null)
                selectedSourceImage.postValue("")
            }

            sourcesSelectionData.postValue(allSources)
            sourceSelectionsListsData.postValue(allLists)
        }
    }

    fun onSourceSelected(sourceSelection: RSSSource) {
        launch {
            when {
                sourceSelection.isFake -> {
                    sourceRepository.unselectAll()
                }
                sourceSelection.isList -> {
                    sourceSelection.listId?.let {
                        sourceRepository.setSelectedList(it)
                    }
                }
                else -> {
                    sourceRepository.setSelected(sourceSelection.URLs.firstOrNull())
                }
            }
            selectedSourceChanged.postValue(true)
            updateSourcesSelection()
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
                    updateSourcesSelection()
                }
            }
        }
    }
}