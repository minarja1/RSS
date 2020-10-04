package cz.minarik.nasapp.utils

import android.content.Context
import cz.minarik.base.common.extensions.booleanPreference
import cz.minarik.base.common.extensions.stringPreference
import cz.minarik.base.common.prefs.PrefManager
import cz.minarik.nasapp.data.model.ArticleFilterType

class UniversePrefManager(context: Context) : PrefManager(context) {

    companion object {
        const val selectedArticleFilterKey = "selectedArticleFilterKey"
        const val showArticleFiltersKey = "showArticleFiltersKey"
    }


    private var articleFilter by stringPreference(
        selectedArticleFilterKey,
        ArticleFilterType.All.key
    )
    var showArticleFilters by booleanPreference(showArticleFiltersKey, false)

    fun getArticleFilter(): ArticleFilterType {
        return ArticleFilterType.fromKey(articleFilter ?: ArticleFilterType.All.key)
            ?: ArticleFilterType.All
    }

    fun setArticleFilter(filter: ArticleFilterType) {
        articleFilter = filter.key
    }
}