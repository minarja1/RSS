package cz.minarik.nasapp.utils

/**
 * Code that should be run when starting the app.
 */
object AppStarter {
    fun run() {
        RemoteConfigHelper.updateDB()
    }
}