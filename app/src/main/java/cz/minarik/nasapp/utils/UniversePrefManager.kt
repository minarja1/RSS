package cz.minarik.nasapp.utils

import android.content.Context
import cz.minarik.base.common.extensions.stringPreference
import cz.minarik.base.common.prefs.PrefManager

class UniversePrefManager(context: Context) : PrefManager(context) {

    companion object {
        const val selectedArticlesSource = "selectedArticlesSource"
    }


//    var selectedArticleSource by stringPreference(selectedArticlesSource, null)

}