package cz.minarik.nasapp.ui.articles.source_selection

import android.view.View
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.DiffUtil
import coil.load
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.ArticleSourceButton
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.ui.base.BaseAdapter
import cz.minarik.nasapp.ui.base.BaseListAdapter
import kotlinx.android.synthetic.main.item_product_section_title.view.*
import kotlinx.android.synthetic.main.row_article_source_button.view.*
import kotlinx.android.synthetic.main.row_source_item.view.*


class TitleAdapter(
    items: List<String> = emptyList(),
    private var onItemClicked: (() -> Unit)?
) : BaseAdapter<String>(
    R.layout.item_product_section_title,
    items,
) {
    override fun bind(itemView: View, item: String, position: Int, viewHolder: BaseViewHolderImp) {
        itemView.run {
            sectionTitleTextView.text = item
            titleBackground.setOnClickListener {
                titleBackground.isClickable = false
                onItemClicked?.invoke()
                arrowImageView.animate()
                    .rotationBy(180f)
                    .withEndAction {
                        titleBackground.isClickable = true
                    }
                    .setDuration(125).interpolator = LinearInterpolator()
            }
        }
    }
}

class ArticleSourceAdapter(
    private var onItemClicked: ((item: RSSSource) -> Unit)?
) : BaseListAdapter<RSSSource>(
    R.layout.row_source_item, object : DiffUtil.ItemCallback<RSSSource>() {
        override fun areItemsTheSame(
            oldItem: RSSSource,
            newItem: RSSSource
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: RSSSource,
            newItem: RSSSource
        ): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bind(
        itemView: View,
        item: RSSSource,
        position: Int,
        viewHolder: BaseViewHolderImp
    ) {
        itemView.run {
            sourceSelectionView.set(item)
            sourceSelectionView.setOnClickListener {
                onItemClicked?.invoke(item)
            }
        }
    }
}

class ArticleSourceButtonAdapter(
    buttons: List<ArticleSourceButton>,
    private var onItemClicked: (() -> Unit)?
) : BaseAdapter<ArticleSourceButton>(
    R.layout.row_article_source_button,
    buttons
) {
    override fun bind(
        itemView: View,
        item: ArticleSourceButton,
        position: Int,
        viewHolder: BaseViewHolderImp
    ) {
        itemView.run {
            sourceButtonBackground.setOnClickListener {
                onItemClicked?.invoke()
            }
            item.imageRes?.let {
                sourceButtonImageView.load(item.imageRes)
            }
            sourceButtonTextView.text = item.title
        }
    }
}