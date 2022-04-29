package com.dn.cleanapi.viewbinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dn.cleanapi.FileUtil
import com.dn.cleanapi.R
import com.dn.cleanapi.databinding.CleanupItemJunkDetailChildBinding
import com.dn.cleanapi.entity.MenuJunkChild


class JunkDetailChildViewHolder : DataBindingViewBinder<MenuJunkChild>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.cleanup_item_junk_detail_child, parent, false))
    }

    private inner class ViewHolder(itemView: View) :
        DataBindingViewBinder.DataBindingViewHolder<MenuJunkChild, CleanupItemJunkDetailChildBinding>(
            itemView
        ), View.OnClickListener {

        init {
            mBinding.itemCheckIv.setOnClickListener(this)
        }

        override fun bindData(t: MenuJunkChild) {
            mBinding.itemNameTv.text = t.iJunkEntity.appName
            val desc = t.iJunkEntity.junkDescription?.description ?: ""
            if (desc.isNotEmpty()) {
                mBinding.itemDescTv.visibility = View.VISIBLE
                mBinding.itemDescTv.text = desc
            } else {
                mBinding.itemDescTv.visibility = View.GONE
            }
            if (t.select){
                mBinding.itemCheckIv.setImageResource(R.drawable.cleanup_icon_check_sel)
            }else{
                mBinding.itemCheckIv.setImageResource(R.drawable.cleanup_icon_check_nor)
            }
            mBinding.itemSizeTv.text = FileUtil.getFileSizeText(t.iJunkEntity.junkSize)
        }

        override fun onClick(v: View) {
            itemClickListener?.apply {
                val position = layoutPosition
                val item = adapter.items[position] as MenuJunkChild
                onItemClick(v, position, item)
            }
        }
    }
}