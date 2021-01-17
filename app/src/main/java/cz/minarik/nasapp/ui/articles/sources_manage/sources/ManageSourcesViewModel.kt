package cz.minarik.nasapp.ui.articles.sources_manage.sources

import androidx.lifecycle.MutableLiveData
import cz.minarik.base.di.base.BaseViewModel
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ManageSourcesViewModel : BaseViewModel() {
    private val sourceDao: RSSSourceDao by inject()
    private val sourceListDao: RSSSourceListDao by inject()

    val sourcesData = MutableLiveData<MutableList<RSSSource>>()
    val sourceListsData = MutableLiveData<MutableList<RSSSource>>()

    init {
        updateSources()
    }

    private fun updateSources() {
        defaultScope.launch {
            val allSources: MutableList<RSSSource> = mutableListOf()
            allSources.addAll(sourceDao.getAll().map {
                RSSSource.fromEntity(it)
            })

            val allLists: MutableList<RSSSource> = mutableListOf()
            allLists.addAll(sourceListDao.getAll().map {
                RSSSource.fromEntity(it)
            })

            sourcesData.postValue(allSources)
            sourceListsData.postValue(allLists)
        }

    }
}