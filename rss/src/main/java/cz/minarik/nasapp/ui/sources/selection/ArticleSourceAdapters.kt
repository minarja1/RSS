package cz.minarik.nasapp.ui.sources.selection

import android.view.View
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.DiffUtil
import cz.minarik.base.ui.base.BaseAdapter
import cz.minarik.base.ui.base.BaseListAdapter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.android.synthetic.main.item_product_section_title.view.*
import kotlinx.android.synthetic.main.row_source_item.view.*
import kotlinx.android.synthetic.main.source_selection_list_item.view.*

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
    private var onItemClicked: ((item: RSSSource) -> Unit)?,
    private var onItemBlocked: ((item: RSSSource) -> Unit)? = null,
    private var onItemInfo: ((item: RSSSource) -> Unit)? = null,
    private var showPopupMenu: Boolean = true,
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
                if (item.isHidden && showPopupMenu) {
                    sourceSelectionView.showPopUp(onItemBlocked, onItemInfo)
                } else {
                    onItemClicked?.invoke(item)
                }
            }
            if (showPopupMenu) {
                sourceSelectionView.setOnLongClickListener {
                    sourceSelectionView.showPopUp(onItemBlocked, onItemInfo)
                    true
                }
            }
        }
    }
}