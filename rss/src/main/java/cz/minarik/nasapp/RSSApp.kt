package cz.minarik.nasapp

import android.app.Application
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.google.android.gms.security.ProviderInstaller
import cz.minarik.nasapp.di.allModules
import cz.minarik.nasapp.utils.AppStarter
import cz.minarik.nasapp.utils.TimberReleaseTree
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber


abstract class RSSApp : Application() {

    init {
        sharedInstance = this
    }

    open val allowSourceManagement = true

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
        AppStarter.run()
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