package cz.minarik.nasapp.utils

import java.util.concurrent.TimeUnit

class Constants {
    companion object {
        val articlesCacheExpiration: Long = TimeUnit.HOURS.toMillis(1)
        val sourcesUpdateGap: Long = TimeUnit.DAYS.toMillis(1)

        const val RECYCLER_MAX_VERTICAL_OFFEST_FOR_SMOOTH_SCROLLING: Long = 1000
        const val ARTICLE_EXPAND_ANIMATION_DURATION: Long = 300L

        const val argArticleDTO = "argArticleDTO"
        const val ARTICLE_BOTTOM_SHEET_TAG = "ARTICLE_BOTTOM_SHEET_TAG"
    }
}