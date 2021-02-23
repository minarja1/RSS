package cz.minarik.spacenews

import cz.minarik.nasapp.RSSApp

class SpaceNewsApp : RSSApp() {
    override val allowSourceManagement = false
    override val dataStoreName = "SpaceNewsDataStore"
}