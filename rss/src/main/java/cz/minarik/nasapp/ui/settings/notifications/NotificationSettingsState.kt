package cz.minarik.nasapp.ui.settings.notifications

import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.NotificationSettings

data class NotificationSettingsState(
    val notificationSettings: NotificationSettings,
    val sources: List<RSSSource>,
)
