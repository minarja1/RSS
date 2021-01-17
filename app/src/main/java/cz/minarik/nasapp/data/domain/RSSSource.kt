package cz.minarik.nasapp.data.domain

import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import cz.minarik.nasapp.data.db.entity.RSSSourceListDataEntity
import cz.minarik.nasapp.utils.compareLists
import java.io.Serializable

data class RSSSource(
    val title: String? = null,
    val URLs: List<String> = emptyList(),
    val imageUrl: String? = null,
    var selected: Boolean = false,
    var listId: Long? = null,
    var isFake: Boolean = false,//"all articles"
    var isList: Boolean = false,
) : Serializable {

    companion object {
        fun fromEntity(
            source: RSSSourceEntity,
        ): RSSSource {
            return RSSSource(
                title = source.title,
                URLs = listOf(source.url),
                imageUrl = source.imageUrl,
                selected = source.isSelected,
                isList = false,
            )
        }

        fun fromEntity(
            source: RSSSourceListDataEntity,
        ): RSSSource {
            return RSSSource(
                title = source.rssSourceEntity.title,
                URLs = source.sources.map {
                    it.url
                },
                imageUrl = source.rssSourceEntity.imageUrl,
                selected = source.rssSourceEntity.isSelected,
                listId = source.rssSourceEntity.id,
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
        return result
    }


}