package cz.minarik.nasapp.ui.articles.source_selection

import android.content.Context
import androidx.lifecycle.MutableLiveData
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import cz.minarik.nasapp.data.model.RSSSourceDTO
import kotlinx.coroutines.launch

class SourceSelectionViewModel(
    private val context: Context,
    val sourceRepository: RSSSourceRepository,
    private val sourceDao: RSSSourceDao,
) : BaseViewModel() {

    val sourcesData = MutableLiveData<MutableList<RSSSourceDTO>>()
    val selectedSource = MutableLiveData<String>()
    val selectedSourceName = MutableLiveData<String?>()
    val selectedSourceImage = MutableLiveData<String>()

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
                selectedSourceName.postValue(selectedSource.title)
            } else {
                selectedSourceName.postValue("")
            }

            selectedSourceImage.postValue(selectedSource?.imageUrl)

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

    fun onSourceSelected(sourceSelectionDTO: RSSSourceDTO) {
        launch {
            sourceRepository.setSelected(sourceSelectionDTO.url)
            selectedSource.postValue(sourceSelectionDTO.url)
            updateSources()
        }
    }

    fun onSourceSelected(sourceUrl: String) {
        launch {
            sourceRepository.setSelected(sourceUrl)
            selectedSource.postValue(sourceUrl)
            updateSources()
        }
    }

    fun hasData(): Boolean {
        return allSources.isEmpty()
    }
}