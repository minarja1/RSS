package cz.minarik.nasapp.ui.base

//todo move to base
interface BaseRecyclerItem {
    fun onDetachedFromWindow(holder: BaseViewHolder<Any>)
    fun onAttachedToWindow(holder: BaseViewHolder<Any>)
}