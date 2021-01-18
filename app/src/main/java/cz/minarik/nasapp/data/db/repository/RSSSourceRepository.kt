package cz.minarik.nasapp.data.db.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseError
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseRepository
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URL
import java.nio.charset.Charset

class RSSSourceRepository(
    private val context: Context,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
    private val prefManager: UniversePrefManager,
) : BaseRepository(),
    RealtimeDatabaseQueryListener<List<RealtimeDatabaseHelper.RssFeedDTO>> {

    companion object {
        const val cacheExpirationMillis = 1000L * 60L * 60L * 24 // 1 day

        fun createFakeListItem(
            context: Context,
            allUrls: List<String>,
            selected: Boolean
        ): RSSSource {
            return RSSSource(
                context.getString(R.string.all_articles),
                allUrls,
                imageUrl = null,
                selected,
                isFake = true,
                isList = true,
            )
        }
    }

    val state = MutableLiveData<NetworkState>()
    val sourcesChanged = MutableLiveData<Boolean>()

    fun updateRSSSourcesFromRealtimeDB() {
        RealtimeDatabaseHelper.getNewsFeeds(this)
    }

    private fun updateDB(allFromServer: List<RealtimeDatabaseHelper.RssFeedDTO?>) {
        state.postValue(NetworkState.LOADING)
        val parser = Parser.Builder()
            .context(context)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(cacheExpirationMillis)
            .build()

        GlobalScope.launch {
            val allDB = sourceDao.getNonUserAdded()

            val dbUrls = allDB.map { it.url }
            val allServerUrls = allFromServer.map { it?.url }

            //delete entries no longer present on server
            for (dbUrl in dbUrls) {
                if (!allServerUrls.contains(dbUrl)) {
                    allDB.find { it.url == dbUrl }?.let {
                        sourceDao.delete(it)
                    }
                }
            }

            val shouldUpdate =
                System.currentTimeMillis() - prefManager.lastSourcesUpdate >= Constants.sourcesUpdateGap

            //create or update existing
            allFromServer.map { feed ->
                async {
                    feed?.url?.let {
                        try {
                            var entity = sourceDao.getByUrl(it)
                            val url = URL(it)
                            if (entity == null) {
                                val channel = parser.getChannel(it)
                                entity = RSSSourceEntity(
                                    url = it,
                                    title = channel.title,
                                    imageUrl = url.getFavIcon()
                                )
                            } else if (shouldUpdate) {
                                val channel = parser.getChannel(it)
                                entity.title = channel.title
                                entity.imageUrl = url.getFavIcon()
                            }

                            sourceDao.insert(entity)
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                }
            }.awaitAll()

            val newDb = sourceDao.getNonUserAdded()
            if (!compareLists(allDB, newDb)) {
                sourcesChanged.postValue(true)
            }
            state.postValue(NetworkState.SUCCESS)
        }
    }

    override fun onDataChange(data: List<RealtimeDatabaseHelper.RssFeedDTO>?) {
        updateDB(data ?: emptyList())
    }

    override fun onCancelled(error: DatabaseError) {
        Timber.e(error.message)
        state.postValue(NetworkState.Companion.error(context.getString(R.string.common_base_error)))
    }

    suspend fun setSelected(selectedUrl: String?) {
        //select given source
        for (source in sourceDao.getAllUnblocked()) {
            source.isSelected = source.url == selectedUrl
            sourceDao.update(source)
        }

        unselectAllLists()
    }

    private suspend fun unselectAllLists() {
        for (list in sourceListDao.getAll()) {
            list.rssSourceEntity.isSelected = false
            sourceListDao.update(list.rssSourceEntity)
        }
    }

    private suspend fun unselectAllSources() {
        for (source in sourceDao.getAllUnblocked()) {
            source.isSelected = false
            sourceDao.update(source)
        }
    }

    suspend fun setSelectedList(id: Long) {
        //select given list
        for (list in sourceListDao.getAll()) {
            list.rssSourceEntity.isSelected = list.rssSourceEntity.id == id
            sourceListDao.update(list.rssSourceEntity)
        }

        unselectAllSources()
    }

    suspend fun unselectAll() {
        unselectAllLists()
        unselectAllSources()
    }
}