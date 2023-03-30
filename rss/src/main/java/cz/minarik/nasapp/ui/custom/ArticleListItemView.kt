package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.*
import cz.minarik.nasapp.R
import cz.minarik.nasapp.RSSApp
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.databinding.ArticleListItemBinding
import cz.minarik.nasapp.utils.Constants
import cz.minarik.nasapp.utils.getValueAnimator
import cz.minarik.nasapp.utils.loadImageWithDefaultSettings
import java.net.URL


class ArticleListItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private var article: ArticleDTO? = null

    private var binding: ArticleListItemBinding

    var articleFullImageView: ImageView

    var onItemExpanded: (() -> Unit)? = null
    var onContactInfoClicked: (() -> Unit)? = null
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

    val expanded: Boolean
        get() = article?.expanded ?: false

    init {
        binding = ArticleListItemBinding.inflate(LayoutInflater.from(context), this, true)
        articleFullImageView = binding.articleFullImageView
        binding.subtitleTextView.handleHTML(context)
        binding.contactInfoTextView.setOnClickListener {
            onContactInfoClicked?.invoke()
        }
    }

    fun set(article: ArticleDTO) {
        this.article = article
        binding.apply {
            expandButton.isVisible = article.expandable
            expandButton.setOnClickListener {
                this@ArticleListItemView.article?.run {
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

            titleTextViewCollapsed.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (article.read) R.color.textColorSecondary else R.color.textColorPrimary
                )
            )

            titleTextViewExpanded.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (article.read) R.color.textColorSecondary else R.color.textColorPrimary
                )
            )
            titleTextViewExpanded.background =
                AppCompatResources.getDrawable(
                    context,
                    if (article.read) R.drawable.gradient_bottom else R.drawable.gradient_bottom_color_surface
                )

            endGradient.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    if (article.read) R.drawable.gradient_end else R.drawable.gradient_end_color_surface
                )
            )


            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (article.read) R.color.colorWindowBackground else R.color.colorSurface
                )
            )

            titleTextViewCollapsed.text = article.title
            titleTextViewExpanded.text = article.title
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
    }

    private fun expandNoAnimation() {
        changeExpandedViewsVisibility()
        binding.cardView.layoutParams?.height =
            if (article?.expanded == true) ViewGroup.LayoutParams.WRAP_CONTENT else collapsedHeight

    }

    private fun preComputeExpandedDimensionsAndExpand() {
        changeExpandedViewsVisibility(true)
        binding.cardView.doOnLayout {
            expandedHeight = binding.cardView.height
            expandedImageWidth = binding.articleFullImageContainer.width
            changeExpandedViewsVisibility()
            animateExpandItem()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.subtitleTextView.setOnClickListener(l) // because it can be html and consumes touches
        binding.cardView.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.subtitleTextView.setOnLongClickListener(l) // because it can be html and consumes touches
        binding.cardView.setOnLongClickListener(l)
    }

    private fun animateExpandItem() {
        article?.run {
            val animator = getValueAnimator(
                expanded, Constants.listItemExpandDuration, AccelerateDecelerateInterpolator()
            ) { progress -> setExpandProgress(progress) }

            animator.doOnStart {
                changeExpandedViewsVisibility(hideTitles = true)
            }
            animator.doOnEnd {
                changeExpandedViewsVisibility()
            }

            animator.start()
        }
    }

    private fun setExpandProgress(progress: Float) {
        if (expandedHeight > 0 && collapsedHeight > 0) {
            binding.cardView.layoutParams?.height =
                (collapsedHeight + (expandedHeight - collapsedHeight) * progress).toInt()

            val width =
                (collapsedImageWidth + (expandedImageWidth - collapsedImageWidth) * progress).toInt()
            binding.articleFullImageContainer.layoutParams.width = width
            articleFullImageView.layoutParams.width = width

            val height =
                (collapsedImageHeight + (expandedImageHeight - collapsedImageHeight) * progress).toInt()
            binding.articleFullImageContainer.layoutParams?.height = height
            articleFullImageView.layoutParams?.height = height
        }

        binding.cardView.requestLayout()
        binding.articleFullImageContainer.requestLayout()

        //todo rotate button?
//        holder.chevron.rotation = 90 * progress
    }

    private fun changeExpandedViewsVisibility(
        fakeExpanded: Boolean? = null,
        hideTitles: Boolean = false
    ) {
        article?.apply {
            val finalExpanded = fakeExpanded ?: expanded
            binding.subtitleTextView.isVisible = finalExpanded

            if (hideTitles) {
                binding.titleTextViewExpanded.isVisible = false
                binding.titleTextViewCollapsed.isVisible = false
            } else {
                binding.titleTextViewExpanded.isVisible = finalExpanded
                binding.titleTextViewCollapsed.isVisible = !finalExpanded
            }

            binding.contentLayoutContainer.orientation = if (finalExpanded) VERTICAL else HORIZONTAL

            val width =
                if (finalExpanded) ViewGroup.LayoutParams.MATCH_PARENT else resources.getDimension(R.dimen.collapsed_image_width)
                    .toInt()
            binding.articleFullImageContainer.layoutParams.width = width
            articleFullImageView.layoutParams.width = width

            val height =
                if (finalExpanded) resources.getDimension(R.dimen.article_list_item_expanded_image_height)
                    .toInt() else ViewGroup.LayoutParams.MATCH_PARENT
            binding.articleFullImageContainer.layoutParams?.height = height
            articleFullImageView.layoutParams?.height = height

            binding.cardView.layoutParams?.height =
                if (finalExpanded) LayoutParams.WRAP_CONTENT else context.resources.getDimension(R.dimen.article_list_item_collapsed_height)
                    .toInt()

            val contentPadding = if (finalExpanded) 16.dpToPx else 8.dpToPx
            binding.contentLayout.setPadding(
                if (finalExpanded) contentPadding else 0,
                contentPadding,
                contentPadding,
                contentPadding
            )

            val contentPaddingSmall = if (finalExpanded) 4.dpToPx else 2.dpToPx
            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, contentPaddingSmall, 0, 0)
            binding.sourceContainer.layoutParams = layoutParams
            binding.dateContainer.setPadding(0, contentPaddingSmall, 0, 0)

            binding.expandButton.load(if (finalExpanded) R.drawable.ic_baseline_keyboard_arrow_up_24 else R.drawable.ic_baseline_keyboard_arrow_down_24)

            binding.contactInfoTextView.isVisible =
                finalExpanded && RSSApp.sharedInstance.hasToComply

            binding.endGradient.isVisible = !finalExpanded
        }
    }
}

