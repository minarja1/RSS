package cz.minarik.nasapp.utils

import android.content.Context

/**
 * Code that should be run when starting the app.
 */
object AppStarter {
    fun run(context: Context) {
        RemoteConfigHelper.updateDB()
        AppStartWorker.run(context)
        UpdateArticlesWorker.run(context)
    }
}