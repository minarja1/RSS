package cz.minarik.techworld

import cz.minarik.nasapp.RSSApp

class TechWorldApp : RSSApp() {
    override val dataStoreName = "TechWorldDataStore"
    override val versionName = BuildConfig.VERSION_NAME
    override val hasToComply = false
}
