package cz.minarik.nasapp.ui.sources.manage.lists

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import cz.minarik.base.ui.base.BaseListAdapter
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import kotlinx.android.synthetic.main.row_source_item.view.*

class ManageListsAdapter(
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