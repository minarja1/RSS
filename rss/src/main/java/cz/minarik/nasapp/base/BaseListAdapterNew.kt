package cz.minarik.nasapp.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import cz.minarik.base.ui.base.BaseRecyclerItem
import cz.minarik.base.ui.base.BaseViewHolder
import java.lang.ClassCastException

//todo move changes to base
abstract class BaseListAdapterNew<T>(
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
            this@BaseListAdapterNew.bind(itemView, item, position, this)
        }
    }

    abstract fun bind(itemView: View, item: T, position: Int, viewHolder: BaseViewHolderImp)

    override fun onViewAttachedToWindow(holder: BaseViewHolder<T>) {
        super.onViewAttachedToWindow(holder)
        val item = holder.itemView.tag
        if (item != null) {
            try {
                itemOnScreen(item as T)
            } catch (e: ClassCastException) {
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<T>) {
        super.onViewDetachedFromWindow(holder)
        val item = holder.itemView.tag
        if (item != null) {
            try {
                itemOffScreen(item as T)
            } catch (e: ClassCastException) {
            }
        }
    }

    open fun itemOnScreen(item: T) {

    }

    open fun itemOffScreen(item: T) {

    }

}