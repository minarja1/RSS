package cz.minarik.nasapp.ui.sources.detail

import androidx.lifecycle.MediatorLiveData
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity

class SourceDetailViewModel(
    private val sourceUrl: String,
    private val dao: RSSSourceDao,
) : BaseViewModel() {

    val sourceLiveData = MediatorLiveData<RSSSourceEntity>()

    init {
        loadSource()
    }

    private fun loadSource() {
        launch {
            sourceLiveData.postValue(dao.getByUrl(sourceUrl))
        }
    }

}