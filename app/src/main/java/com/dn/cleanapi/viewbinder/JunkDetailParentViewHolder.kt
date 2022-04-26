package com.dn.cleanapi.viewbinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dn.cleanapi.FileUtil
import com.dn.cleanapi.R
import com.dn.cleanapi.databinding.CleanupItemJunkDetailParentBinding
import com.dn.cleanapi.entity.MenuJunkParent


class JunkDetailParentViewHolder : DataBindingViewBinder<MenuJunkParent>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.cleanup_item_junk_detail_parent, parent, false))
    }

    private inner class ViewHolder(itemView: View) :
        DataBindingViewBinder.DataBindingViewHolder<MenuJunkParent, CleanupItemJunkDetailParentBinding>(
            itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
            mBinding.itemCheckIv.setOnClickListener(this)
        }

        override fun bindData(t: MenuJunkParent) {
            mBinding.itemNameTv.text = t.name
            mBinding.itemSizeTv.text = FileUtil.getFileSizeText(t.size)
            if (t.isExpand){
                mBinding.itemExpandIv.setImageResource(R.drawable.cleanup_icon_arrow_up)
            }else{
                mBinding.itemExpandIv.setImageResource(R.drawable.cleanup_icon_arrow_down)
            }
        }

        override fun onClick(v: View) {
            itemClickListener?.apply {
                val position = layoutPosition
                val item = adapter.items[position] as MenuJunkParent
                onItemClick(v, position, item)
            }
        }
    }
}