package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["url"]),
    ]
)
data class RSSSourceEntity(
    @PrimaryKey
    var url: String,

    var title: String? = null,
    var description: String? = null,
    var homePage: String? = null,
    var contactUrl: String? = null,
    var imageUrl: String? = null,
    var isUserAdded: Boolean = false,
    var isSelected: Boolean = false,
    var isHidden: Boolean = false,
    var forceOpenExternally: Boolean = false,
    var isAtom: Boolean = false,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RSSSourceEntity

        if (url != other.url) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (homePage != other.homePage) return false
        if (contactUrl != other.contactUrl) return false
        if (imageUrl != other.imageUrl) return false
        if (isUserAdded != other.isUserAdded) return false
        if (isSelected != other.isSelected) return false
        if (isHidden != other.isHidden) return false
        if (forceOpenExternally != other.forceOpenExternally) return false
        if (isAtom != other.isAtom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (homePage?.hashCode() ?: 0)
        result = 31 * result + (contactUrl?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + isUserAdded.hashCode()
        result = 31 * result + isSelected.hashCode()
        result = 31 * result + isHidden.hashCode()
        result = 31 * result + forceOpenExternally.hashCode()
        result = 31 * result + isAtom.hashCode()
        return result
    }
}