package cz.minarik.nasapp.data.domain

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import java.io.Serializable

@Stable
@Immutable
data class RSSSourceNotificationListItem(
    val title: String? = null,
    val URLs: List<String> = emptyList(),
    val imageUrl: String? = null,
    val notificationsEnabled: Boolean = false,
) : Serializable {

    companion object {
        fun fromEntity(
            entity: RSSSourceEntity,
        ): RSSSourceNotificationListItem {
            return RSSSourceNotificationListItem(
                title = entity.title,
                URLs = listOf(entity.url),
                imageUrl = entity.imageUrl,
                notificationsEnabled = entity.isNotificationsEnabled,
            )
        }
    }
}