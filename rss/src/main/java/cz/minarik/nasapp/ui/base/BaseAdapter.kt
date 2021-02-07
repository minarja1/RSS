package cz.minarik.nasapp.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

//todo move to base
abstract class BaseAdapter<T>(
        private val itemLayoutRes: Int,
        var items: List<T>,
) : RecyclerView.Adapter<BaseViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false)
        return BaseViewHolderImp(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return itemLayoutRes
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = items[position]
        //set item as tag of view to make it retrievable
        holder.itemView.tag = item
        holder.bind(item, position)
    }

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

    inner class BaseViewHolderImp(itemView: View) : BaseViewHolder<T>(itemView) {
        override fun bind(item: T, position: Int) {
            this@BaseAdapter.bind(itemView, item, position, this)
        }
    }

    abstract fun bind(itemView: View, item: T, position: Int, viewHolder: BaseViewHolderImp)
}