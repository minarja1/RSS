package cz.minarik.nasapp.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

//todo move to base
abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T, position: Int)
}

