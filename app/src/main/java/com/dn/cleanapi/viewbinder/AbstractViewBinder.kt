package com.dn.cleanapi.viewbinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder


abstract class AbstractViewBinder<T, VH : RecyclerView.ViewHolder> : ItemViewBinder<T, VH>() {

    var clickListener : View.OnClickListener? = null

    var itemClickListener: OnItemClickListener<T>? = null

    var itemLongClickListener: OnItemLongClickListener<T>? = null

    fun createView(inflater: LayoutInflater, parent: ViewGroup, layoutId: Int) =
        inflater.inflate(layoutId, parent, false)

    interface OnItemClickListener<T> {
        fun onItemClick(view: View, position: Int, t: T)
    }

    interface OnItemLongClickListener<T> {
        fun onLongItemClick(view: View, position: Int, t: T): Boolean
    }
}