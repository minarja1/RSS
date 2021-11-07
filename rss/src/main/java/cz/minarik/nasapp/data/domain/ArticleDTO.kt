package cz.minarik.nasapp.data.domain

import android.text.style.ImageSpan
import androidx.core.text.getSpans
import cz.minarik.base.common.extensions.getHostFromUrl
import cz.minarik.base.common.extensions.toHtml
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.utils.removeImagesFromHtml
import java.io.Serializable
import java.util.*

data class ArticleDTO(
    var guid: String? = null,
    var title: String? = null,
    val image: String? = null,
    val date: Date? = null,
    var link: String? = null,
    var description: String? = null,
    var audio: String? = null,
    var video: String? = null,
    var sourceName: String? = null,
    var sourceUrl: String? = null,
    var categories: MutableList<String> = mutableListOf(),
    var read: Boolean = false,
    var expanded: Boolean = false,
    var expandable: Boolean = true,
    var starred: Boolean = false,
    var domain: String? = null,
    var showSource: Boolean = true,
) : Serializable {

    override fun toString(): String {
        return title ?: super.toString()
    }

    val isValid: Boolean
        get() {
            return !link.isNullOrEmpty() && !title.isNullOrEmpty()
        }

    companion object {
        fun fromDb(article: ArticleEntity): ArticleDTO {
            val image = if (article.image.isNullOrEmpty()) {
                article.description?.toHtml()?.getSpans<ImageSpan>()?.getOrNull(0)?.source ?: ""
            } else {
                article.image
            }

            return ArticleDTO(
                starred = article.starred,
                guid = article.guid,
                title = article.title,
                image = image,
                date = article.date,
                link = article.link,
                description = article.description?.removeImagesFromHtml(),
                audio = article.audio,
                video = article.video,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
                categories = article.categories,
                domain = article.link?.getHostFromUrl(),
                expandable = true,
                read = article.read,
            )
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleDTO

        if (guid != other.guid) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (date != other.date) return false
        if (link != other.link) return false
        if (description != other.description) return false
        if (audio != other.audio) return false
        if (video != other.video) return false
        if (sourceName != other.sourceName) return false
        if (sourceUrl != other.sourceUrl) return false
        if (categories != other.categories) return false
        if (read != other.read) return false
        if (expandable != other.expandable) return false
        if (expanded != other.expanded) return false
        if (starred != other.starred) return false
        if (domain != other.domain) return false
        if (showSource != other.showSource) return false
        if (read != other.read) return false

        return true
    }

    override fun hashCode(): Int {
        var result = guid?.hashCode() ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (audio?.hashCode() ?: 0)
        result = 31 * result + (video?.hashCode() ?: 0)
        result = 31 * result + (sourceName?.hashCode() ?: 0)
        result = 31 * result + (sourceUrl?.hashCode() ?: 0)
        result = 31 * result + categories.hashCode()
        result = 31 * result + read.hashCode()
        result = 31 * result + expandable.hashCode()
        result = 31 * result + expanded.hashCode()
        result = 31 * result + starred.hashCode()
        result = 31 * result + (domain?.hashCode() ?: 0)
        result = 31 * result + (showSource?.hashCode() ?: 0)
        result = 31 * result + (read?.hashCode() ?: 0)
        return result
    }

}