package cz.minarik.technews

import cz.minarik.nasapp.RSSApp

class TechNewsApp : RSSApp() {
    override val dataStoreName = "TechNewsDataStore"
    override val versionName = BuildConfig.VERSION_NAME
    override val hasToComply = false
}