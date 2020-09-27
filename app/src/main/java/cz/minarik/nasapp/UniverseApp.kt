package cz.minarik.nasapp

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import cz.minarik.nasapp.di.allModules
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class UniverseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@UniverseApp)
            modules(allModules)
        }
        initCoilImageLoader()
    }

    private fun initCoilImageLoader() {
        Coil.setDefaultImageLoader(
            ImageLoader(this) {
                okHttpClient {
                    // Initialized lazily on a background thread.
                    //todo doresit https
//                    ProviderInstaller.installIfNeeded(this@NASApp)

                    OkHttpClient.Builder()
                        .cache(CoilUtils.createDefaultCache(this@UniverseApp))
                        .build()
                }
            }
        )
    }

}