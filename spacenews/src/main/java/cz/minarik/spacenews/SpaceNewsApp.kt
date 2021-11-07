package cz.minarik.spacenews

import cz.minarik.nasapp.RSSApp

class SpaceNewsApp : RSSApp() {
    override val dataStoreName = "SpaceNewsDataStore"
    override val versionName = BuildConfig.VERSION_NAME
    override val hasToComply = false
}