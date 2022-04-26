package com.dn.cleanapi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dn.cleanapi.databinding.CleanupFragmentJunkDetailBinding
import com.dn.cleanapi.entity.MenuJunkChild
import com.dn.cleanapi.entity.MenuJunkParent
import com.dn.cleanapi.viewbinder.AbstractViewBinder
import com.dn.cleanapi.viewbinder.JunkDetailChildViewHolder
import com.dn.cleanapi.viewbinder.JunkDetailParentViewHolder
import com.drakeet.multitype.MultiTypeAdapter
import com.mckj.api.entity.AppJunk

/**
 *
create by leix on 2022/4/25
desc:
 */
class JunkDetailActivity:AppCompatActivity() {

    private lateinit var mBinding: CleanupFragmentJunkDetailBinding

    private lateinit var mModel:JunkDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,R.layout.cleanup_fragment_junk_detail)
        mBinding.junkDetailRecycler.layoutManager = LinearLayoutManager(this)
        mModel = getViewModel()
        initData()
    }



    private fun initData(){
        val appJunk = DataTransport.getInstance().get("junk_list") as MutableList<AppJunk>
        mModel.init(appJunk)
        mModel.mDetailLiveData.observe(this) {
            setAdapter(it)
        }
    }



    fun getViewModel(): JunkDetailViewModel {
        return ViewModelProvider(this, JunkDetailViewModel.JunkDetailViewModelFactory()).get(
            JunkDetailViewModel::class.java
        )
    }

    private fun setAdapter(list: List<Any>) {
        if (mBinding.junkDetailRecycler.adapter == null) {
            mBinding.junkDetailRecycler.adapter = mAdapter
        }
        mAdapter.items = list
        mAdapter.notifyDataSetChanged()
    }
    private val mAdapter by lazy {
        val adapter = MultiTypeAdapter()
        adapter.register(MenuJunkParent::class.java, JunkDetailParentViewHolder().also {
            it.itemClickListener = object : AbstractViewBinder.OnItemClickListener<MenuJunkParent> {
                override fun onItemClick(view: View, position: Int, t: MenuJunkParent) {
                    when (view.id) {
                        R.id.item_check_iv -> {
                            //选中
                            mModel.select(t)
                        }
                        else -> {
                            mModel.expand(t)
                        }
                    }
                }
            }
        })
        adapter.register(MenuJunkChild::class, JunkDetailChildViewHolder().also {
            it.itemClickListener = object : AbstractViewBinder.OnItemClickListener<MenuJunkChild> {
                override fun onItemClick(view: View, position: Int, t: MenuJunkChild) {
                    mModel.select(t)
                }
            }
        })
        adapter
    }
}