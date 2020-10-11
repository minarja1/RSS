package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.core.view.isVisible
import coil.api.load
import com.prof.rssparser.Article
import cz.minarik.base.common.extensions.dpToPx
import cz.minarik.base.common.extensions.toDateFromRSS
import cz.minarik.base.common.extensions.toHtml
import cz.minarik.base.common.extensions.toShortFormat
import cz.minarik.nasapp.R
import cz.minarik.nasapp.utils.getHostFromUrl
import cz.minarik.nasapp.utils.handleHTML
import cz.minarik.nasapp.utils.loadImageWithDefaultSettings
import kotlinx.android.synthetic.main.article_list_item.view.*
import java.util.*


class ArticleListItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private var article: ArticleDTO? = null
    var articleImageView: ImageView

    init {
        inflate(context, R.layout.article_list_item, this)
        articleImageView = findViewById(R.id.articleImageView)
        subtitleTextView.handleHTML(context)
    }

    fun set(article: ArticleDTO) {
        this.article = article
        expandLayout.isVisible = article.expandable
        expandLayout.setOnClickListener {
            article.expanded = !article.expanded
            expand()
        }

        titleTextView.setTypeface(null, if (article.read) Typeface.NORMAL else Typeface.BOLD)
        subtitleTextView.setTextColor(
            ContextCompat.getColor(
                context,
                if (article.read) R.color.textColorSecondary else R.color.textColorPrimary
            )
        )
        cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                if (article.read) R.color.colorWindowBackground else R.color.colorSurface
            )
        )

        titleTextView.text = article.title
        dateTextView.text = article.date?.toShortFormat()

        articleImageView.loadImageWithDefaultSettings(article.image?.replace("http://", "https://"))
        articleFullImageView.loadImageWithDefaultSettings(
            article.image?.replace(
                "http://",
                "https://"
            )
        )

        articleImageView.transitionName = article.image
        subtitleTextView.text = article.description

        domainTextView.text = article.domain
        domainDividerTextView.isVisible = !article.domain.isNullOrEmpty()

        expand()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        subtitleTextView.setOnClickListener(l) // because it can be html and consumes touches
        cardView.setOnClickListener(l)
    }

    private fun expand() {
        article?.run {
            subtitleTextView.isVisible = expanded
            articleImageView.isVisible = !expanded
            articleFullImageView.isVisible = expanded
            cardView.layoutParams = cardView.layoutParams.apply {
                width = LayoutParams.MATCH_PARENT
                height = if (expanded) LayoutParams.WRAP_CONTENT else 110.dpToPx
            }

            expandImageView.load(if (expanded) R.drawable.ic_baseline_keyboard_arrow_up_24 else R.drawable.ic_baseline_keyboard_arrow_down_24)
            invalidate()
            requestLayout()
        }
    }
}

data class ArticleDTO(
    var guid: String? = null,
    var title: String? = null,
    val image: String? = null,
    val date: Date? = null,
    var link: String? = null,
    var description: Spanned? = null,
    var content: String? = null,
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
) {

    override fun toString(): String {
        return title?: super.toString()
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
        if (content != other.content) return false
        if (audio != other.audio) return false
        if (video != other.video) return false
        if (sourceName != other.sourceName) return false
        if (sourceUrl != other.sourceUrl) return false
        if (categories != other.categories) return false
        if (read != other.read) return false
        if (expandable != other.expandable) return false
        if (starred != other.starred) return false
        if (domain != other.domain) return false

        return true
    }

    override fun hashCode(): Int {
        var result = guid?.hashCode() ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (audio?.hashCode() ?: 0)
        result = 31 * result + (video?.hashCode() ?: 0)
        result = 31 * result + (sourceName?.hashCode() ?: 0)
        result = 31 * result + (sourceUrl?.hashCode() ?: 0)
        result = 31 * result + categories.hashCode()
        result = 31 * result + read.hashCode()
        result = 31 * result + expandable.hashCode()
        result = 31 * result + starred.hashCode()
        result = 31 * result + (domain?.hashCode() ?: 0)
        return result
    }

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
                description = article.description?.toHtml(),
                content = article.content?.toHtml().toString(),
                audio = article.audio,
                video = article.video,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
                categories = article.categories,
                domain = article.link?.getHostFromUrl(),
            )
        }
    }



}