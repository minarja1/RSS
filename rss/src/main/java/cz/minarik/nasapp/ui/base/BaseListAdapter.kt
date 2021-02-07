package cz.minarik.nasapp.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

//todo move to base
abstract class BaseListAdapter<T>(
        private val itemLayoutRes: Int,
        diffCallback: DiffUtil.ItemCallback<T>,
) : ListAdapter<T, BaseViewHolder<T>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false)
        return BaseViewHolderImp(view)
    }

    override fun getItemViewType(position: Int): Int {
        return itemLayoutRes
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = getItem(position)
        //set item as tag of view to make it retrievable
        holder.itemView.tag = item
        holder.bind(item, position)
    }

    inner class BaseViewHolderImp(itemView: View) : BaseViewHolder<T>(itemView) {
        override fun bind(item: T, position: Int) {
            this@BaseListAdapter.bind(itemView, item, position, this)
        }
    }

    abstract fun bind(itemView: View, item: T, position: Int, viewHolder: BaseViewHolderImp)

    override fun onViewAttachedToWindow(holder: BaseViewHolder<T>) {
        super.onViewAttachedToWindow(holder)
        val item = holder.itemView.tag
        if (item != null && item is BaseRecyclerItem) {
            item.onAttachedToWindow(holder as BaseViewHolder<Any>)
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<T>) {
        super.onViewDetachedFromWindow(holder)
        val item = holder.itemView.tag
        if (item != null && item is BaseRecyclerItem) {
            item.onDetachedFromWindow(holder  as BaseViewHolder<Any>)
        }
    }
}