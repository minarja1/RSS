package cz.minarik.nasapp.data.db.entity

import android.text.Spanned
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.minarik.nasapp.ui.custom.ArticleDTO
import java.util.*

@Entity
data class StarredArticleEntity(
    @PrimaryKey
    var guid: String,
    var title: String? = null,
    val image: String? = null,
    val date: Date? = null,
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
        fun fromModel(article: ArticleDTO): StarredArticleEntity {
            return StarredArticleEntity(
                guid = article.guid ?: "",
                title = article.title,
                image = article.image,
                date = article.date,
                link = article.link,
                description = article.description.toString(),
                content = article.content,
                audio = article.audio,
                video = article.video,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
                categories = article.categories,
                read = article.read,
                starred = article.starred,
                domain = article.domain
            )
        }
    }

}