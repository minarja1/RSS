package cz.minarik.nasapp.utils

import android.content.Context
import cz.minarik.base.common.extensions.booleanPreference
import cz.minarik.base.common.extensions.longPreference
import cz.minarik.base.common.extensions.stringPreference
import cz.minarik.base.common.prefs.PrefManager
import cz.minarik.nasapp.data.domain.ArticleFilterType

class RSSPrefManager(context: Context) : PrefManager(context) {

    companion object {
        const val selectedArticleFilterKey = "selectedArticleFilterKey"
        const val lastSourcesUpdateFilterKey = "lastSourcesUpdateFilterKey"
        const val showArticleFiltersKey = "showArticleFiltersKey"
    }


    private var articleFilter by stringPreference(
        selectedArticleFilterKey,
        ArticleFilterType.All.key
    )

    var lastSourcesUpdate by longPreference(lastSourcesUpdateFilterKey)

    fun getArticleFilter(): ArticleFilterType {
        return ArticleFilterType.fromKey(articleFilter ?: ArticleFilterType.All.key)
            ?: ArticleFilterType.All
    }

    fun setArticleFilter(filter: ArticleFilterType) {
        articleFilter = filter.key
    }
}