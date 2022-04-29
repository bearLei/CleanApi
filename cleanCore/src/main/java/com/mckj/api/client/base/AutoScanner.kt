package com.mckj.api.client.base

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mckj.api.client.JunkConstants
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.manager.CacheDbOption

/**
 *
create by leix on 2022/4/29
desc: 自动监听回调
 */
class AutoScanner(scanTime: Long) : LifecycleEventObserver {
    companion object {
        const val TAG = "AutoCleaner"
    }


    val cacheJunkLiveData = MutableLiveData<ScanBean>()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                subscribeCacheStatus(source)
            }
            Lifecycle.Event.ON_DESTROY -> {

            }
        }
    }


    private fun subscribeCacheStatus(source: LifecycleOwner) {
        CacheDbOption.getCacheByType(JunkConstants.Session.APP_CACHE)?.observe(source) {
            Log.d(
                TAG,
                "缓存扫描状态：${it.scanBean?.status}\n扫描大小${it.scanBean?.junk?.junkSize}\n更新时间${it.updateTime}"
            )
            if (it?.scanBean == null) return@observe
            it.scanBean?.apply {
                cacheJunkLiveData.postValue(this)
            }
        }
    }


}