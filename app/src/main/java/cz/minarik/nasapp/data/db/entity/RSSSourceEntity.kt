package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import timber.log.Timber

@Entity
data class RSSSourceEntity(
    @PrimaryKey
    var url: String,

    var title: String? = null,
    var imageUrl: String? = null,
    var isUserAdded: Boolean = false,
    var isSelected: Boolean = false,

    ) {
    override fun equals(other: Any?): Boolean {
        Timber.i("RSSSourceEntity: comparing $this with $other")
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RSSSourceEntity

        if (url != other.url) return false
        if (title != other.title) return false
        if (imageUrl != other.imageUrl) return false
        if (isUserAdded != other.isUserAdded) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + isUserAdded.hashCode()
        result = 31 * result + isSelected.hashCode()
        return result
    }
}