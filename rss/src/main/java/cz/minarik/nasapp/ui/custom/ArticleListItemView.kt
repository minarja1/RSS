package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.*
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.utils.Constants
import cz.minarik.nasapp.utils.getValueAnimator
import cz.minarik.nasapp.utils.loadImageWithDefaultSettings
import kotlinx.android.synthetic.main.article_list_item.view.*
import java.io.Serializable
import java.net.URL
import java.util.*


class ArticleListItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private var article: ArticleDTO? = null

    var articleFullImageView: ImageView

    var onItemExpanded: (() -> Unit)? = null
    var filterBySource: ((url: String?) -> Unit)? = null


    private val collapsedHeight =
        context.resources.getDimension(R.dimen.article_list_item_collapsed_height).toInt()
    private var expandedHeight = -1 // will be calculated dynamically

    private val collapsedImageWidth =
        context.resources.getDimension(R.dimen.collapsed_image_width).toInt()
    private var expandedImageWidth = -1 // will be calculated dynamically

    private val collapsedImageHeight =
        context.resources.getDimension(R.dimen.article_list_item_collapsed_height).toInt()
    private val expandedImageHeight =
        context.resources.getDimension(R.dimen.article_list_item_expanded_image_height).toInt()


    init {
        inflate(context, R.layout.article_list_item, this)
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
            preComputeExpandedDimensionsAndExpand()
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

        expandNoAnimation()
    }

    private fun expandNoAnimation() {
        changeExpandedViewsVisibility()
        cardView.layoutParams.height =
            if (article?.expanded == true) ViewGroup.LayoutParams.WRAP_CONTENT else collapsedHeight

    }

    private fun preComputeExpandedDimensionsAndExpand() {
        changeExpandedViewsVisibility(true)
        cardView.doOnLayout {
            expandedHeight = cardView.height
            expandedImageWidth = articleFullImageView.width
            changeExpandedViewsVisibility()
            animateExpandItem()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        subtitleTextView.setOnClickListener(l) // because it can be html and consumes touches
        cardView.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        subtitleTextView.setOnLongClickListener(l) // because it can be html and consumes touches
        cardView.setOnLongClickListener(l)
    }

    private fun animateExpandItem() {
        article?.run {
            val animator = getValueAnimator(
                expanded, Constants.listItemExpandDuration, AccelerateDecelerateInterpolator()
            ) { progress -> setExpandProgress(progress) }

            if (expanded) animator.doOnStart {
                changeExpandedViewsVisibility()
            }
            else animator.doOnEnd {
                changeExpandedViewsVisibility()
            }

            animator.start()
        }
    }

    private fun setExpandProgress(progress: Float) {
        if (expandedHeight > 0 && collapsedHeight > 0) {
            cardView.layoutParams.height =
                (collapsedHeight + (expandedHeight - collapsedHeight) * progress).toInt()

            articleFullImageView.layoutParams.width =
                (collapsedImageWidth + (expandedImageWidth - collapsedImageWidth) * progress).toInt()

            articleFullImageView.layoutParams.height =
                (collapsedImageHeight + (expandedImageHeight - collapsedImageHeight) * progress).toInt()
        }

        cardView.requestLayout()
        articleFullImageView.requestLayout()

        //todo rotate button?
//        holder.chevron.rotation = 90 * progress
    }

    private fun changeExpandedViewsVisibility(fakeExpanded: Boolean? = null) {
        article?.run {
            val finalExpanded = fakeExpanded ?: expanded
            subtitleTextView.isVisible = finalExpanded

            contentLayoutContainer.orientation = if (finalExpanded) VERTICAL else HORIZONTAL
            articleFullImageView.layoutParams.width =
                if (finalExpanded) ViewGroup.LayoutParams.MATCH_PARENT else resources.getDimension(R.dimen.collapsed_image_width)
                    .toInt()

            articleFullImageView.layoutParams.height =
                if (finalExpanded) resources.getDimension(R.dimen.article_list_item_expanded_image_height)
                    .toInt() else ViewGroup.LayoutParams.MATCH_PARENT

            cardView.layoutParams.height =
                if (finalExpanded) LayoutParams.WRAP_CONTENT else context.resources.getDimension(R.dimen.article_list_item_collapsed_height)
                    .toInt()

            val contentPadding = if (finalExpanded) 16.dpToPx else 8.dpToPx
            contentLayout.setPadding(contentPadding, contentPadding, contentPadding, contentPadding)

            val contentPaddingSmall = if (finalExpanded) 4.dpToPx else 2.dpToPx
            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, contentPaddingSmall, 0, 0)
            sourceContainer.layoutParams = layoutParams
            dateContainer.setPadding(0, contentPaddingSmall, 0, 0)

            expandButton.load(if (finalExpanded) R.drawable.ic_baseline_keyboard_arrow_up_24 else R.drawable.ic_baseline_keyboard_arrow_down_24)
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
    var openExternally: Boolean = false,
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
        if (openExternally != other.openExternally) return false

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
        result = 31 * result + (openExternally?.hashCode() ?: 0)
        result = 31 * result + (read?.hashCode() ?: 0)
        return result
    }

}