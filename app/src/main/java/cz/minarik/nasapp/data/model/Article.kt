package cz.minarik.nasapp.data.model

import com.prof.rssparser.Article
import me.toptas.rssconverter.RssItem
import java.io.Serializable

data class Article(
    var guid: String? = null,
    var title: String? = null,
    var author: String? = null,
    var link: String? = null,
    var pubDate: String? = null,
    var description: String? = null,
    var content: String? = null,
    var image: String? = null,
    var audio: String? = null,
    var video: String? = null,
    var sourceName: String? = null,
    var sourceUrl: String? = null,
    private var _categories: MutableList<String> = mutableListOf()
) : Serializable {

    companion object {
        fun fromLibrary(article: Article): cz.minarik.nasapp.data.model.Article {
            return cz.minarik.nasapp.data.model.Article(
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

        fun fromApi(article: RssItem): cz.minarik.nasapp.data.model.Article {
            val guid = "${article.link}|${article.publishDate}"
            return cz.minarik.nasapp.data.model.Article(
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