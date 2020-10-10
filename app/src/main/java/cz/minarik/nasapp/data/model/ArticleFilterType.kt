package cz.minarik.nasapp.data.model

import cz.minarik.nasapp.R

enum class ArticleFilterType(val key: String, val chipId: Int) {
    All("all", R.id.filterAll), Unread("unread", R.id.filterUnread), Starred(
        "starred",
        R.id.filterStarred
    );

    companion object {
        fun fromKey(key: String) = values().firstOrNull { it.key == key }
    }

}