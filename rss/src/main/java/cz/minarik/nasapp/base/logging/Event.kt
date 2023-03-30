package cz.minarik.nasapp.base.logging

sealed class Event {

    data class ArticleClicked(
        val link: String?,
    ) : Event()

    data class ArticleMarkedAsRead(
        val link: String?,
        val read: Boolean,
    ) : Event()

    data class ArticleStarred(
        val link: String?,
        val starred: Boolean,
    ) : Event()

    data class ArticleLongClicked(
        val link: String?,
    ) : Event()


    data class ArticleShared(
        val link: String?,
    ) : Event()

    data class SimpleArticlesClicked(
        val sourceUrl: String,
    ) : Event()

    data class SourceDetailOpened(
        val sourceUrl: String,
    ) : Event()

    object SettingsOpened : Event()

    object AboutOpened : Event()

    object ArticleExpanded : Event()

    object NewPostsCardClicked : Event()

    object SourcesSelectionOpened : Event()

    data class ArticleFilterApplied(
        val filterType: String,
    ) : Event()

    data class FilterBySearchQuery(
        val query: String?
    ) : Event()

    data class SourceBlocked(
        val sourceUrl: String,
        val blocked: Boolean
    ) : Event()

    class ArticleOpenedInBrowser(
        val link: String?
    ) : Event()

}