package cz.minarik.nasapp.ui.sources.selection

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import cz.minarik.base.ui.base.BaseListAdapter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.ui.custom.SourceSelectionItemView

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
            val sourceSelectionView =
                itemView.findViewById<SourceSelectionItemView>(R.id.sourceSelectionView)
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