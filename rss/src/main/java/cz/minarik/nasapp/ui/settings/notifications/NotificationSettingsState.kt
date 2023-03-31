package cz.minarik.nasapp.ui.settings.notifications

import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.utils.NotificationSettings

/**
 * State for notifications settings screen.
 *
 * @property notificationSettings Notification settings.
 * @property sourcesWithNotificationsOn List of sources with notifications on.
 */
data class NotificationSettingsState(
    val notificationSettings: NotificationSettings,
    val sourcesWithNotificationsOn: List<RSSSource>,
)
