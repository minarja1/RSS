package cz.minarik.nasapp.data.db.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.prof.rssparser.Parser
import cz.minarik.base.common.extensions.compareLists
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.data.NetworkState
import cz.minarik.base.di.base.BaseRepository
import cz.minarik.base.di.createOkHttpClient
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.RssFeedDTO
import cz.minarik.nasapp.utils.createCall
import cz.minarik.nasapp.utils.toSyncFeed
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.URL
import java.nio.charset.Charset


class RSSSourceRepository(
    private val context: Context,
    private val sourceDao: RSSSourceDao,
) : BaseRepository() {

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

    fun updateDB(
        allFromServer: List<RssFeedDTO?>,
        onSuccess: (() -> Unit)? = null,
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

            //create or update existing
            allFromServer.map { feed ->
                async {

                    feed?.url?.let { feedUrl ->
                        try {
                            var entity = sourceDao.getByUrl(feedUrl)
                            val url = URL(feedUrl)
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
                                        isSelected = entity?.isSelected ?: false,
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
                                    isSelected = entity?.isSelected ?: false,
                                )
                            }

                            entity?.let { sourceDao.insert(it) }
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
            DataStoreManager.setInitialSyncFinished(true)
            onSuccess?.invoke()
        }
    }

}