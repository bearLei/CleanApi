package com.mckj.api.client.base

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mckj.api.client.JunkConstants
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.manager.CacheDbOption
import com.mckj.api.util.ScopeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 *
create by leix on 2022/4/29
desc: 首页自动监听回调
 */
class HomeAutoScanner(var type: Int = JunkConstants.Session.APP_CACHE, var atLeastTime: Long) :
    LifecycleEventObserver {
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

    /**
     * 订阅db数据
     */
    private fun subscribeCacheStatus(source: LifecycleOwner) {
        CacheDbOption.getCacheLiveDataByType(JunkConstants.Session.APP_CACHE)?.observe(source) {
            if (it?.scanBean == null) return@observe
            it.scanBean?.apply {
                cacheJunkLiveData.postValue(this)
            }
        }
    }


    /**
     * 首页扫描
     * a.数据库中已有缓存且未失效，则读取数据库中的数据进行
     *  a.1 控制扫描时间
     *  a.2 根据app颗粒列表计算每个颗粒的delay时间
     * b.缓存读取失败/过期，开启真实扫描
     */
    fun scan() {
        ScopeHelper.launch {
            withContext(Dispatchers.IO) {
                val dbData = CacheDbOption.getCacheByType(JunkConstants.Session.APP_CACHE)
                if (!checkCacheAvailable(dbData)) {
                    Log.d(TAG, "缓存失效")
                    JunkClient.instance.scan(JunkConstants.Session.APP_CACHE)
                    return@withContext
                }
                dbData?.scanBean?.let {
                    val junk = it.junk
                    val appJunks = junk.appJunks
                    if (junk.junkSize <= 0 || appJunks.isNullOrEmpty()) return@withContext
                    val cacheJunk = CacheJunk(junkSize = 0L, appJunks = mutableListOf())
                    val scanBean = ScanBean(junk = cacheJunk)
                    scanBean.status = JunkConstants.ScanStatus.START
                    cacheJunkLiveData.postValue(scanBean)
                    val delayTime = calculateDelayTime(atLeastTime, appJunks.size)
                    appJunks.forEach { appJunk ->
                        cacheJunk.appJunks?.add(appJunk)
                        cacheJunk.junkSize += appJunk.junkSize
                        scanBean.status = JunkConstants.ScanStatus.SCAN_IDLE
                        cacheJunkLiveData.postValue(scanBean)
                        delay(delayTime)
                    }
                    scanBean.status = JunkConstants.ScanStatus.COMPLETE
                    cacheJunkLiveData.postValue(scanBean)
                } ?: let {
                    JunkClient.instance.scan(JunkConstants.Session.APP_CACHE)
                }
            }
        }
    }


    /**
     * 缓存是否可用
     */
    private fun checkCacheAvailable(cacheDb: CacheDb?): Boolean {
        if (cacheDb?.scanBean?.junk?.appJunks.isNullOrEmpty()
            || cacheDb?.scanBean?.junk?.junkSize!! <= 0L
        ) return false
        val updateTime = cacheDb.updateTime
        if (System.currentTimeMillis() - updateTime >= 1 * 60 * 1000) {
            return false
        }
        return true
    }

    /**
     * 每个app颗粒的delay时间
     */
    private fun calculateDelayTime(scanTime: Long, appSize: Int = 0): Long {
        return scanTime / appSize
    }

}