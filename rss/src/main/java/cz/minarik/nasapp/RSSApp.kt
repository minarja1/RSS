package cz.minarik.nasapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.google.android.gms.security.ProviderInstaller
import cz.minarik.nasapp.di.allModules
import cz.minarik.nasapp.utils.AppStarter
import cz.minarik.nasapp.utils.Constants.Companion.notificationChannelId
import cz.minarik.nasapp.utils.NotificationHelper
import cz.minarik.nasapp.utils.TimberReleaseTree
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber


abstract class RSSApp : Application() {

    init {
        sharedInstance = this
    }

    companion object {
        lateinit var sharedInstance: RSSApp
            private set

        val applicationContext: Context
            get() = sharedInstance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RSSApp)
            modules(allModules)
        }
        initCoilImageLoader()
        initTimber()
        createNotificationChannel()
        AppStarter.run(applicationContext)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
                vibrationPattern = NotificationHelper.vibrationPattern
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(TimberReleaseTree())
        }
    }

    private fun initCoilImageLoader() {
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient {
                    // Initialized lazily on a background thread.
                    ProviderInstaller.installIfNeeded(this@RSSApp)

                    OkHttpClient.Builder()
                        .cache(CoilUtils.createDefaultCache(this@RSSApp))
                        .build()
                }.build()
        )
    }

    abstract val dataStoreName: String

    abstract val versionName: String

    abstract val hasToComply: Boolean
}