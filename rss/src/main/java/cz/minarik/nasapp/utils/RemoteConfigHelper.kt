package cz.minarik.nasapp.utils

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import cz.minarik.base.common.extensions.moshi
import cz.minarik.nasapp.BuildConfig
import cz.minarik.nasapp.data.db.repository.RSSSourceRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

object RemoteConfigHelper : KoinComponent {

    val repository: RSSSourceRepository by inject()

    const val feedsKey = "RSSFeeds"

    init {
        val configSettings = remoteConfigSettings {
            if (BuildConfig.DEBUG)
                minimumFetchIntervalInSeconds = 1
        }
        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun updateDB() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                repository.updateDB(getRSSFeeds())
                Timber.i("${javaClass.name}, successfully fetched")
            } else {
                Timber.i("${javaClass.name}, fetch failed")
            }
        }
    }

    private fun getRSSFeeds(): List<RssFeedDTO> {
        val feedsJson = Firebase.remoteConfig.getString(feedsKey)

        if (feedsJson.isEmpty()) return emptyList()

        val jsonAdapter: JsonAdapter<RSSFeedsContainer> =
            moshi().adapter(
                Types.newParameterizedType(
                    RSSFeedsContainer::class.java,
                    List::class.java,
                    RssFeedDTO::class.java
                )
            )

        Timber.i("${javaClass.name}, RSSFeeds from RemoteConfig: $feedsJson")
        return jsonAdapter.fromJson(feedsJson)?.rssFeeds ?: emptyList()
    }
}

data class RSSFeedsContainer(
    val rssFeeds: List<RssFeedDTO>?
)