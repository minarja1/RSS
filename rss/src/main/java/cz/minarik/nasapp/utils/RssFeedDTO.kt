package cz.minarik.nasapp.utils

data class RssFeedDTO(
    val homePage: String? = null,
    val contact: String? = null,
    val url: String? = null,
    val forceOpenExternal: Boolean = false,
    val atom: Boolean = false,
)