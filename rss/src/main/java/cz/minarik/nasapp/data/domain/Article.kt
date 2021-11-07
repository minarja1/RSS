package cz.minarik.nasapp.data.domain

import com.prof.rssparser.Article
import com.rometools.rome.feed.synd.SyndEntry
import cz.minarik.base.common.extensions.toDateFromRSS
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.utils.addDays
import cz.minarik.nasapp.utils.guid
import kotlinx.coroutines.flow.first
import me.toptas.rssconverter.RssItem
import java.io.Serializable
import java.util.*

data class Article(
    var guid: String? = null,
    var title: String? = null,
    var author: String? = null,
    var link: String? = null,
    var pubDate: String? = null,
    var publicationDate: Date? = null,
    var description: String? = null,
    var content: String? = null,
    var image: String? = null,
    var audio: String? = null,
    var video: String? = null,
    var sourceName: String? = null,
    var sourceUrl: String? = null,
    private var _categories: MutableList<String> = mutableListOf()
) : Serializable {

    val formattedDate: Date?
        get() = publicationDate ?: pubDate?.toDateFromRSS()

    fun isValid(maxDate: Date): Boolean {
        val ageValid: Boolean = formattedDate?.after(maxDate) ?: false
        return ageValid && !guid.isNullOrEmpty() && formattedDate != null
    }

    companion object {
        fun fromLibrary(article: Article): cz.minarik.nasapp.data.domain.Article {
            return cz.minarik.nasapp.data.domain.Article(
                guid = article.guid,
                title = article.title,
                author = article.author,
                link = article.link,
                pubDate = article.pubDate,
                description = article.description,
                content = article.content,
                image = article.image,
                audio = article.audio,
                video = article.video,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
            )
        }

        fun fromLibrary(
            article: SyndEntry,
            sourceUrl: String?,
            sourceName: String?
        ): cz.minarik.nasapp.data.domain.Article {
            return Article(
                guid = article.guid(),
                title = article.title,
                author = article.author,
                link = article.link,
                publicationDate = article.publishedDate ?: article.updatedDate,
                description = article.description?.value,
                sourceUrl = sourceUrl,
                sourceName = sourceName,
            )
        }

        fun fromApi(article: RssItem): cz.minarik.nasapp.data.domain.Article {
            val guid = "${article.link}|${article.publishDate}"
            return cz.minarik.nasapp.data.domain.Article(
                guid = guid,
                title = article.title,
                link = article.link,
                pubDate = article.publishDate,
                description = article.description,
                image = article.image,
            )
        }
    }

    val categories: MutableList<String>
        get() = _categories

    fun addCategory(category: String) {
        _categories.add(category)
    }
}