package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import com.prof.rssparser.Article
import cz.minarik.base.common.extensions.toDateFromRSS
import cz.minarik.base.common.extensions.toHtml
import cz.minarik.base.common.extensions.toShortFormat
import cz.minarik.nasapp.R
import cz.minarik.nasapp.utils.loadImageWithDefaultSettings
import java.util.*


class ArticleListItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val articleBackground: ViewGroup
    val articleImageView: ImageView
    private val titleTextView: TextView
    private val dateTextView: TextView
    private val articleStateImageView: ImageView

    init {
        inflate(context, R.layout.article_list_item, this)

        articleBackground = findViewById(R.id.articleBackground)
        articleImageView = findViewById(R.id.articleImageView)
        titleTextView = findViewById(R.id.titleTextView)
        dateTextView = findViewById(R.id.dateTextView)
        articleStateImageView = findViewById(R.id.articleStateImageView)
    }

    fun set(article: ArticleDTO) {
        titleTextView.text = article.title
        dateTextView.text = article.date?.toShortFormat()

        articleStateImageView.setColorFilter(
            ContextCompat.getColor(
                context,
                if (article.read) R.color.green else R.color.textColorSecondary
            ), android.graphics.PorterDuff.Mode.SRC_IN
        )

        articleImageView.loadImageWithDefaultSettings(article.image?.replace("http://", "https://"))
        articleImageView.transitionName = article.image

        invalidate()
        requestLayout()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        articleBackground.setOnClickListener(l)
    }
}

data class ArticleDTO(
    var guid: String? = null,
    val title: String? = null,
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
) {
    val isValid: Boolean
        get() {
            return !link.isNullOrEmpty() && !title.isNullOrEmpty()
        }

    companion object {
        fun fromApi(article: Article): ArticleDTO {
            val image = if (article.image.isNullOrEmpty()) {
                article.description?.toHtml()?.getSpans<ImageSpan>()?.getOrNull(0)?.source ?: ""
            } else {
                article.image
            }

            val date = article.pubDate?.toDateFromRSS()

            return ArticleDTO(
                guid = article.guid,
                title = article.title,
                image = image,
                date = date,
                link = article.link,
                description = article.description,
                content = article.content,
                audio = article.audio,
                video = article.video,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
                categories = article.categories,
            )
        }
    }
}