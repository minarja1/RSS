package cz.minarik.nasapp.ui.custom

import android.animation.LayoutTransition
import android.content.Context
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.dpToPx
import cz.minarik.base.common.extensions.getFavIcon
import cz.minarik.base.common.extensions.toDateFromRSS
import cz.minarik.base.common.extensions.toHtml
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.utils.*
import kotlinx.android.synthetic.main.article_list_item.view.*
import java.io.Serializable
import java.net.URL
import java.util.*


class ArticleListItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private var article: ArticleDTO? = null
    var articleImageView: ImageView
    var articleFullImageView: ImageView

    var onItemExpanded: (() -> Unit)? = null
    var filterBySource: ((url: String?) -> Unit)? = null

    init {
        inflate(context, R.layout.article_list_item, this)
        articleImageView = findViewById(R.id.articleImageView)
        articleFullImageView = findViewById(R.id.articleFullImageView)
        subtitleTextView.handleHTML(context)
    }

    fun set(article: ArticleDTO) {
        this.article = article
        expandButton.isVisible = article.expandable
        expandButton.setOnClickListener {
            this.article?.run {
                expanded = !expanded
            }
            expand()
        }

        subtitleTextView.setTextColor(
            ContextCompat.getColor(
                context,
                if (article.read) R.color.textColorSecondary else R.color.textColorPrimary
            )
        )

        titleTextView.setTextColor(
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
        dateTextView.text = article.date?.toTimeElapsed()

        articleImageView.loadImageWithDefaultSettings(
            article.image?.replace("http://", "https://"),
            crossFade = true
        )

        articleFullImageView.loadImageWithDefaultSettings(
            article.image?.replace(
                "http://",
                "https://"
            ),
            crossFade = true
        )

        subtitleTextView.text = article.description?.toHtml()

        sourceNameTextView.text = article.sourceName

        try {
            val url = URL(article.sourceUrl)
            sourceImageView.load(url.getFavIcon())
        } catch (e: Exception) {
        }

        starImageView.isVisible = article.starred

        sourceContainer.isVisible = article.showSource

        expand(true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        subtitleTextView.setOnClickListener(l) // because it can be html and consumes touches
        cardView.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        subtitleTextView.setOnLongClickListener(l) // because it can be html and consumes touches
        cardView.setOnLongClickListener(l)
    }

    private fun expand(expandingProgrammatically: Boolean = false) {
        article?.run {
            subtitleTextView.isVisible = expanded
            articleImageContainer.isVisible = !expanded
            articleFullImageContainer.isVisible = expanded

            val layoutTransition = cardView.layoutTransition
            if (expanded && !expandingProgrammatically) {
                layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                layoutTransition.setDuration(Constants.ARTICLE_EXPAND_ANIMATION_DURATION)
                layoutTransition.setInterpolator(
                    LayoutTransition.CHANGING,
                    DecelerateInterpolator(1.5f)
                )
                onItemExpanded?.invoke()
            } else {
                layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
            }

            cardView.layoutParams.height =
                if (expanded) LayoutParams.WRAP_CONTENT else context.resources.getDimension(R.dimen.article_list_item_collapsed_height)
                    .toInt()
            cardView.requestLayout()

            val contentPadding = if (expanded) 16.dpToPx else 8.dpToPx
            contentLayout.setPadding(contentPadding, contentPadding, contentPadding, contentPadding)

            val contentPaddingSmall = if (expanded) 4.dpToPx else 2.dpToPx
            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, contentPaddingSmall, 0, 0)
            sourceContainer.layoutParams = layoutParams
            dateContainer.setPadding(0,contentPaddingSmall, 0,0)

            expandButton.load(if (expanded) R.drawable.ic_baseline_keyboard_arrow_up_24 else R.drawable.ic_baseline_keyboard_arrow_down_24)

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
    var description: String? = null,
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
                description = article.description,
                content = article.content?.toHtml().toString(),
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
        if (content != other.content) return false
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
        result = 31 * result + (content?.hashCode() ?: 0)
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