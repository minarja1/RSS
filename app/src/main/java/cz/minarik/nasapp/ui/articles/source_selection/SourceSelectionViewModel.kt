package cz.minarik.nasapp.ui.articles.source_selection

import android.content.Context
import androidx.lifecycle.MutableLiveData
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.model.RSSSourceDTO
import kotlinx.coroutines.launch

class SourceSelectionViewModel(
    private val context: Context,
    val sourceRepository: RSSSourceRepository,
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    val sourcesData = MutableLiveData<MutableList<RSSSourceDTO>>()
    val selectedSource = MutableLiveData<RSSSourceDTO>()
    val selectedSourceName = MutableLiveData<String>()

    private val allSources: MutableList<RSSSourceDTO> = mutableListOf()

    init {
        sourceRepository.updateRSSSourcesFromRealtimeDB()
        updateSources()
    }

    fun updateSources() {
        defaultScope.launch {
            allSources.clear()
            allSources.addAll(sourceDao.getAll().map {
                RSSSourceDTO.fromEntity(it)
            })

            val selectedSource = allSources.firstOrNull { it.selected }
            if (selectedSource?.isList == false) {
                selectedSourceName.postValue(selectedSource?.title)
            } else {
                selectedSourceName.postValue("")
            }

            //"all articles" on top
            allSources.add(
                0, RSSSourceDTO(
                    context.getString(R.string.all_articles),
                    null,
                    imageUrl = null,
                    selectedSource == null
                )
            )
            sourcesData.postValue(allSources)
        }
    }

    fun getSources(): List<RSSSourceDTO> {
        val allSelection = mutableListOf<RSSSourceDTO>()
        val selectedSources = allSources.filter { it.selected }

        allSelection.addAll(allSources.filter { !it.selected })

        //"all articles" on top
        allSelection.add(
            0, RSSSourceDTO(
                context.getString(R.string.all_articles),
                null,
                imageUrl = null,
                selectedSources.isEmpty()
            )
        )

        //selected item as second
        allSelection.addAll(
            1, selectedSources
        )

        return allSelection
    }

    fun onSourceSelected(sourceSelectionDTO: RSSSourceDTO) {
        launch {
            sourceRepository.setSelected(sourceSelectionDTO.url)
            selectedSource.postValue(sourceSelectionDTO)
            updateSources()
        }
    }

    fun hasData(): Boolean {
        return allSources.isEmpty()
    }
}