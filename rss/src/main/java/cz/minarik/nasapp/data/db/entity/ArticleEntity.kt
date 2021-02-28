package cz.minarik.nasapp.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import cz.minarik.nasapp.data.domain.Article
import cz.minarik.nasapp.ui.custom.ArticleDTO
import java.util.*

@Entity(primaryKeys = ["guid", "date"],
    indices = [
        Index(value = ["guid"]),
        Index(value = ["date"]),
        Index(value = ["sourceUrl"]),
    ]
)
data class ArticleEntity(
    var guid: String,
    val date: Date,
    var title: String? = null,
    var image: String? = null,
    var link: String? = null,
    var description: String? = null,
    var content: String? = null,
    var audio: String? = null,
    var video: String? = null,
    var sourceName: String? = null,
    var sourceUrl: String? = null,
    var categories: MutableList<String> = mutableListOf(),
    var read: Boolean = false,
    var starred: Boolean = false,
    var domain: String? = null,
) {
    companion object {
        fun fromModel(article: ArticleDTO): ArticleEntity {
            return ArticleEntity(
                guid = article.guid ?: "",
                title = article.title,
                image = article.image,
                date = article.date ?: Date(),
                link = article.link,
                description = article.description.toString(),
                content = article.content,
                audio = article.audio,
                video = article.video,
                categories = article.categories,
                read = article.read,
                starred = article.starred,
                domain = article.domain,
                sourceUrl = article.sourceUrl,
            )
        }

        fun fromModel(article: Article): ArticleEntity {
            return ArticleEntity(
                guid = article.guid ?: "",
                title = article.title,
                image = article.image,
                date = article.formattedDate ?: Date(),
                link = article.link,
                description = article.description,
                content = article.content,
                audio = article.audio,
                video = article.video,
                categories = article.categories,
                sourceUrl = article.sourceUrl,
                sourceName = article.sourceName,
            )
        }
    }

}