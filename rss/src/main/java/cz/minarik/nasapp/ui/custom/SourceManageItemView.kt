package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import cz.minarik.base.common.extensions.tint
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.android.synthetic.main.source_management_list_item.view.*
import kotlinx.android.synthetic.main.source_selection_list_item.view.sourceCard
import kotlinx.android.synthetic.main.source_selection_list_item.view.sourceImageView
import kotlinx.android.synthetic.main.source_selection_list_item.view.sourceNameTextView


class SourceManageItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    var onShow: ((source: RSSSource) -> Unit)? = null
    var onAdd: ((source: RSSSource) -> Unit)? = null
    var onBlock: ((source: RSSSource) -> Unit)? = null
    lateinit var source: RSSSource

    init {
        inflate(context, R.layout.source_management_list_item, this)
    }

    fun set(
        source: RSSSource,
        onShow: (source: RSSSource) -> Unit,
        onAdd: (source: RSSSource) -> Unit,
        onBlock: (source: RSSSource) -> Unit
    ) {
        this.source = source
        this.onShow = onShow
        this.onAdd = onAdd
        this.onBlock = onBlock

        sourceNameTextView.text = source.title

        when {
            !source.imageUrl.isNullOrEmpty() -> {
                sourceImageView.load(source.imageUrl) {
                    fallback(R.drawable.ic_baseline_article_24)
                }
            }
            source.isList -> {
                sourceImageView.load(R.drawable.ic_baseline_article_24)
            }
            else -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.comet_24px)
                drawable?.tint(context, R.color.textColorPrimary)
                sourceImageView.load(drawable)
            }
        }

        blockTextView.text =
            context.getString(if (source.isHidden) R.string.show else R.string.hide)
        addToListButton.isVisible = !source.isHidden
        blockTextView.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                context,
                if (source.isHidden) R.drawable.ic_baseline_add_circle_outline_24 else R.drawable.ic_baseline_not_interested_24
            ), null, null, null
        )

        sourceImageView.alpha = if(source.isHidden) .5f else 1f

        sourceNameTextView.setTextColor(
            ContextCompat.getColor(
                context,
                if (source.isHidden) R.color.textColorSecondary else R.color.textColorPrimary
            )
        )


        sourceCard.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                if (source.selected) R.color.colorSurface else R.color.colorBackground
            )
        )

        showButton.setOnClickListener { this.onShow?.invoke(source) }
        addToListButton.setOnClickListener { this.onAdd?.invoke(source) }
        blockButton.setOnClickListener { this.onBlock?.invoke(source) }

        invalidate()
        requestLayout()
    }

}