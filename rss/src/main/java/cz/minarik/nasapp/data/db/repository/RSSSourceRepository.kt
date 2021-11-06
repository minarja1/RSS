package cz.minarik.nasapp.data.db.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseError
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.compareLists
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseRepository
import cz.minarik.base.di.createOkHttpClient
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.dao.RSSSourceListDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.net.URL
import java.nio.charset.Charset


class RSSSourceRepository(
    private val context: Context,
    private val sourceDao: RSSSourceDao,
    private val sourceListDao: RSSSourceListDao,
) : BaseRepository(),
    RealtimeDatabaseQueryListener<List<RealtimeDatabaseHelper.RssFeedDTO>> {

    companion object {
        const val cacheExpirationMillis = 1000L * 60L * 60L * 24 // 1 day

        fun createFakeListItem(
            title: String,
            allUrls: List<String>,
            selected: Boolean
        ): RSSSource {
            return RSSSource(
                title,
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

    private val okHttpClient by lazy {
        createOkHttpClient()
    }

    fun updateRSSSourcesFromRealtimeDB(onSuccess: (() -> Unit)?) {
        RealtimeDatabaseHelper.getNewsFeeds(this, onSuccess)
    }

    private fun updateDB(
        allFromServer: List<RealtimeDatabaseHelper.RssFeedDTO?>,
        onSuccess: (() -> Unit)?
    ) {
        state.postValue(NetworkState.LOADING)
        val parser = Parser.Builder()
            .context(context)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(cacheExpirationMillis)
            .build()

        CoroutineScope(Dispatchers.Default).launch {
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

            val shouldUpdate = System.currentTimeMillis() - DataStoreManager.getLastSourcesUpdate()
                .first() >= Constants.sourcesUpdateGap

            //create or update existing
            allFromServer.map { feed ->
                async {

                    feed?.url?.let { feedUrl ->
                        try {
                            var entity = sourceDao.getByUrl(feedUrl)
                            val url = URL(feedUrl)
                            if (entity == null || shouldUpdate) {
                                if (feed.atom) {
                                    okHttpClient.createCall(feedUrl).execute().use { response ->
                                        val channel = response.toSyncFeed()
                                        entity = RSSSourceEntity(
                                            url = feedUrl,
                                            title = channel?.title,
                                            description = channel?.description,
                                            imageUrl = url.getFavIcon(),
                                            homePage = feed.homePage,
                                            contactUrl = feed.contact,
                                            forceOpenExternally = feed.forceOpenExternal,
                                            isAtom = feed.atom,
                                            isHidden = entity?.isHidden ?: false,
                                            isSelected = entity?.isSelected?:false,
                                        )
                                    }
                                } else {
                                    val channel = parser.getChannel(feedUrl)
                                    entity = RSSSourceEntity(
                                        url = feedUrl,
                                        title = channel.title,
                                        description = channel.description,
                                        imageUrl = url.getFavIcon(),
                                        homePage = feed.homePage,
                                        contactUrl = feed.contact,
                                        forceOpenExternally = feed.forceOpenExternal,
                                        isAtom = feed.atom,
                                        isHidden = entity?.isHidden ?: false,
                                        isSelected = entity?.isSelected?:false,
                                        )
                                }
                            }

                            entity?.let { sourceDao.insert(it) }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                }
            }.awaitAll()

            if (shouldUpdate) DataStoreManager.setLastSourcesUpdate(System.currentTimeMillis())

            val newDb = sourceDao.getNonUserAdded()
            if (!compareLists(allDB, newDb)) {
                sourcesChanged.postValue(true)
            }
            state.postValue(NetworkState.SUCCESS)
            DataStoreManager.setInitialSyncFinished(true)
            onSuccess?.invoke()
        }
    }

    override fun onDataChange(
        data: List<RealtimeDatabaseHelper.RssFeedDTO>?,
        onSuccess: (() -> Unit)?
    ) {
        updateDB(data ?: emptyList(), onSuccess)
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

    suspend fun unSelectAll() {
        unselectAllLists()
        unselectAllSources()
    }
}