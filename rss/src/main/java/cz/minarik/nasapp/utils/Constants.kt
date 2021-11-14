package cz.minarik.nasapp.utils

import java.util.concurrent.TimeUnit

class Constants {
    companion object {
        val articlesCacheExpiration: Long = TimeUnit.HOURS.toMillis(1)
        const val argArticleDTO = "argArticleDTO"
        const val argSourceUrl = "argSourceUrl"
        const val ARTICLE_BOTTOM_SHEET_TAG = "ARTICLE_BOTTOM_SHEET_TAG"

        val listItemExpandDuration: Long get() = 300L
        val circularRevealDuration: Long get() = 300L

        const val notificationChannelId = "articles_nitfication_channel"
        const val notificationId = 69
    }
}