package cz.minarik.nasapp.model

import cz.minarik.nasapp.data.db.entity.RSSSourceEntity

data class RSSSourceDTO(
    val title: String? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    var selected: Boolean = false,
) {
    companion object {
        fun fromEntity(
            source: RSSSourceEntity,
        ): RSSSourceDTO {
            return RSSSourceDTO(
                source.title,
                source.url,
                imageUrl = source.imageUrl,
                selected = source.isSelected,
            )
        }
    }
}