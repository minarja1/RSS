package cz.minarik.nasapp.base.logging

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import cz.minarik.nasapp.BuildConfig
import timber.log.Timber

class FirebaseAnalyticsLogger : Logger {

    private val firebaseAnalytics = Firebase.analytics

    override fun logEvent(event: Event) {
        if (BuildConfig.DEBUG) {
            Timber.i(
                "FirebaseAnalyticsLogger: logEvent: event: $event"
            )
        } else {
            when (event) {
                is Event.ArticleClicked -> {
                    firebaseAnalytics.logEvent("article_clicked") {
                        param("article_link", event.link.toString())
                    }
                }
                Event.AboutOpened -> firebaseAnalytics.logEvent("about_opened") {}
                Event.ArticleExpanded -> firebaseAnalytics.logEvent("article_expanded") {}
                is Event.ArticleFilterApplied -> {
                    firebaseAnalytics.logEvent("article_filter_applied") {
                        param("filter_type", event.filterType)
                    }
                }
                is Event.ArticleLongClicked -> {
                    firebaseAnalytics.logEvent("article_long_clicked") {
                        param("article_link", event.link.toString())
                    }
                }
                is Event.ArticleMarkedAsRead -> {
                    firebaseAnalytics.logEvent("article_marked_as_read") {
                        param("article_link", event.link.toString())
                    }
                }
                is Event.ArticleShared -> {
                    firebaseAnalytics.logEvent("article_shared") {
                        param("article_link", event.link.toString())
                    }
                }
                is Event.ArticleStarred -> {
                    firebaseAnalytics.logEvent("article_starred") {
                        param("article_link", event.link.toString())
                    }
                }
                is Event.FilterBySearchQuery -> {
                    firebaseAnalytics.logEvent("filter_by_search_query") {
                        param("query", event.query.toString())
                    }
                }
                Event.NewPostsCardClicked -> firebaseAnalytics.logEvent("new_posts_card_clicked") {}
                Event.SettingsOpened -> firebaseAnalytics.logEvent("settings_opened") {}
                is Event.SimpleArticlesClicked -> {
                    firebaseAnalytics.logEvent("simple_articles_clicked") {
                        param("source_url", event.sourceUrl)
                    }
                }
                is Event.SourceBlocked -> {
                    firebaseAnalytics.logEvent("source_blocked") {
                        param("source_url", event.sourceUrl)
                    }
                }
                is Event.SourceDetailOpened -> {
                    firebaseAnalytics.logEvent("source_detail_opened") {
                        param("source_url", event.sourceUrl)
                    }
                }
                Event.SourcesSelectionOpened -> firebaseAnalytics.logEvent("sources_selection_opened") {}
                is Event.ArticleOpenedInBrowser -> {
                    firebaseAnalytics.logEvent("article_opened_in_browser") {
                        param("article_link", event.link.toString())
                    }
                }
            }
        }
    }

}