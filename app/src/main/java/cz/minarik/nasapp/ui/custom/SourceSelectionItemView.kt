package cz.minarik.nasapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.api.load
import com.google.android.material.card.MaterialCardView
import cz.minarik.nasapp.R
import cz.minarik.nasapp.model.RSSSourceDTO
import cz.minarik.nasapp.utils.tint


class SourceSelectionItemView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val sourceNameTextView: TextView
    private val sourceBackground: ViewGroup
    private val sourceCard: MaterialCardView
    private val sourceImageView: ImageView

    init {
        inflate(context, R.layout.source_selection_list_item, this)

        sourceNameTextView = findViewById(R.id.sourceNameTextView)
        sourceBackground = findViewById(R.id.sourceBackground)
        sourceCard = findViewById(R.id.sourceCard)
        sourceImageView = findViewById(R.id.sourceImageView)
    }

    fun set(source: RSSSourceDTO) {
        sourceNameTextView.text = source.title

        when {
            !source.imageUrl.isNullOrEmpty() -> {
                sourceImageView.load(source.imageUrl){
                    fallback(R.drawable.ic_baseline_filter_list_24)
                }
            }
            source.isList -> {
                sourceImageView.load(R.drawable.ic_baseline_filter_list_24)
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