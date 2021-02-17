package cz.minarik.nasapp.data.domain

import cz.minarik.base.common.extensions.compareLists
import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceListDataEntity
import java.io.Serializable

data class RSSSource(
    val title: String? = null,
    val URLs: List<String> = emptyList(),
    val imageUrl: String? = null,
    var selected: Boolean = false,
    var listId: Long? = null,
    var isFake: Boolean = false,//"all articles"
    var isList: Boolean = false,
    var isBlocked: Boolean = false,
    var openExternally: Boolean = false
) : Serializable {

    companion object {
        fun fromEntity(
            entity: RSSSourceEntity,
        ): RSSSource {
            return RSSSource(
                title = entity.title,
                URLs = listOf(entity.url),
                imageUrl = entity.imageUrl,
                selected = entity.isSelected,
                isList = false,
                isBlocked = entity.isHidden,
                openExternally = entity.forceOpenExternally
            )
        }

        fun fromEntity(
            entity: RSSSourceListDataEntity,
        ): RSSSource {
            return RSSSource(
                title = entity.rssSourceEntity.title,
                URLs = entity.sources.filter { !it.isHidden }.map {
                    it.url
                },
                imageUrl = entity.rssSourceEntity.imageUrl,
                selected = entity.rssSourceEntity.isSelected,
                listId = entity.rssSourceEntity.id,
                isList = true,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RSSSource) return false

        if (title != other.title) return false
        if (!compareLists(URLs, other.URLs)) return false
        if (imageUrl != other.imageUrl) return false
        if (selected != other.selected) return false
        if (listId != other.listId) return false
        if (isFake != other.isFake) return false
        if (isList != other.isList) return false
        if (isBlocked != other.isBlocked) return false
        if (openExternally != other.openExternally) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + URLs.hashCode()
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + selected.hashCode()
        result = 31 * result + (listId?.hashCode() ?: 0)
        result = 31 * result + isFake.hashCode()
        result = 31 * result + isList.hashCode()
        result = 31 * result + openExternally.hashCode()
        result = 31 * result + isBlocked.hashCode()
        return result
    }


}