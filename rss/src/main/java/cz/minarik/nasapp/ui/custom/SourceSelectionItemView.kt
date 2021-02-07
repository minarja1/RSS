package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import coil.load
import cz.minarik.base.common.extensions.tint
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.android.synthetic.main.source_selection_list_item.view.*


class SourceSelectionItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.source_selection_list_item, this)
    }

    fun set(source: RSSSource) {
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

        sourceCard.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                if (source.selected) R.color.colorSurface else R.color.colorBackground
            )
        )

        invalidate()
        requestLayout()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        sourceBackground.setOnClickListener(l)
    }
}