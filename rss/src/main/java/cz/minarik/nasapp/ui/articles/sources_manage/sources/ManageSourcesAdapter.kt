package cz.minarik.nasapp.ui.articles.sources_manage.sources

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.domain.RSSSource
import cz.minarik.nasapp.ui.base.BaseListAdapter
import kotlinx.android.synthetic.main.row_manage_source_item.view.*


class ManageSourcesAdapter(
    var onShow: ((source: RSSSource) -> Unit),
    var onAdd: ((source: RSSSource) -> Unit),
    var onBlock: ((source: RSSSource, position: Int) -> Unit),
) : BaseListAdapter<RSSSource>(
    R.layout.row_manage_source_item, object : DiffUtil.ItemCallback<RSSSource>() {
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
            sourceManageView.set(item, onShow, onAdd, onBlock = {
                onBlock.invoke(it, position)
            })
        }
    }
}
