package cz.minarik.nasapp.data.model

import cz.minarik.nasapp.data.db.entity.RSSSourceEntity
import java.io.Serializable

data class RSSSourceDTO(
    val title: String? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    var selected: Boolean = false,
) : Serializable {

    //todo update when lists added
    val isList: Boolean
        get() = url == null

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