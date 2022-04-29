package com.mckj.api.client.base

import android.util.Log
import androidx.lifecycle.LiveData
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.impl.ICleanCallBack
import com.mckj.api.client.impl.IClientAbility
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.JunkExecutor
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.JunkInfo
import com.mckj.api.entity.ScanBean
import com.mckj.api.init.JunkInitializer
import com.mckj.api.manager.CacheDbOption
import com.mckj.api.util.FileUtils
import com.mckj.api.util.RFileUtils

class JunkClient : IClientAbility {

    companion object {
        const val TAG = "JunkClient"
        val instance: JunkClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { JunkClient() }
    }

    /**
     * @param executorType 执行器类型
     * @param iScanCallBack 扫描回调
     */
    override fun scan(executorType: Int, iScanCallBack: IScanCallBack) {
        realScan(executorType)
    }

    /**
     * @param executorType 执行器类型
     * @return
     */
    override fun scan(executorType: Int): LiveData<CacheDb>? {
        CacheDbOption.getCacheByType(executorType)?.let {
            Log.d(TAG, "数据库命中缓存对象")
            return it
        } ?: let {
            Log.d(TAG, "数据库未命中缓存对象:执行文件扫描")
            realScan(executorType)
            return null
        }
    }

    /**
     * 清理
     * @param list 移除的文件列表
     * @param iCleanCallBack 清理回调
     */
    override fun clean(list: MutableList<JunkInfo>, iCleanCallBack: ICleanCallBack) {
        var removeSizeTotal = 0L
        val removeJunks = mutableListOf<JunkInfo>()
        list.iterator().run {
            iCleanCallBack.cleanStart()
            Log.d(TAG, "cleanStart!")
            while (hasNext()) {
                val next = next()
                if (!delete(next)) {
                    continue
                }
                removeSizeTotal += next.junkSize
                removeJunks.add(next)
                Log.d(TAG, "清理中：${next.path}\n大小：${next.junkSize}")
                iCleanCallBack.cleanIdle(next)
            }
            Log.d(TAG, "清理结束")
            notifyDbCache(removeJunks)
            iCleanCallBack.cleanEnd(removeSizeTotal, removeJunks)
        }
    }

    override fun silentClean(list: MutableList<JunkInfo>) {

    }

    override fun stop(executor: JunkExecutor) {

    }


    /**
     * 移除文件
     */
    private fun delete(junkInfo: JunkInfo): Boolean {
        junkInfo.uri?.let {
            return RFileUtils.deleteFile(it)
        } ?: let {
            return FileUtils.delete(junkInfo.path)
        }
    }

    /**
     * 更新缓存数据库
     */
    private fun notifyDbCache(junks: MutableList<JunkInfo>) {
        val allCache = CacheDbOption.getAllCache()
        if (allCache.isNullOrEmpty()) {
            Log.d(TAG, "notify:allCache is null or empty")
            return
        }
        allCache.forEach {
            it.scanBean?.junk?.let { cacheJunk ->
                cacheJunk.appJunks?.forEach { appJunk ->
                    val iterator = appJunk.junks?.iterator()
                    while (iterator?.hasNext() == true) {
                        val next = iterator.next()
                        if (junks.contains(next)) {
                            iterator.remove()
                            appJunk.junkSize = appJunk.junkSize - next.junkSize
                            cacheJunk.junkSize = cacheJunk.junkSize - next.junkSize
                        }
                    }
                }
            }
            CacheDbOption.insertCache(it)
        }
    }


    /**
     * @param executor
     * 文件扫描执行器
     */
    fun realScan(type: Int) {
        val executor = ExecutorManager.getInstance().getExecutor(type) ?: return
        var cacheDb: CacheDb? = null
        CacheDbOption.getCacheByType(type)?.let {
            it.value?.apply {
                cacheDb = this
            }
        }
        if (cacheDb == null) {
            cacheDb = CacheDb()
            cacheDb?.executorType = executor.mType
        }
        val cacheJunk = CacheJunk(junkSize = 0L, appJunks = mutableListOf())
        val scanBean = ScanBean(junk = cacheJunk)
        cacheDb?.scanBean = scanBean
        try {
            executor.scan(object : IScanCallBack {
                override fun scanStart() {
                    Log.d(
                        TAG,
                        "执行器：${executor.mType}\nscanStart...执行线程${Thread.currentThread().name}"
                    )
                    scanBean.status = JunkConstants.ScanStatus.START
                    CacheDbOption.insertCache(cacheDb)
                }

                override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {
                    scanBean.status = JunkConstants.ScanStatus.COMPLETE
                    Log.d(
                        TAG,
                        "执行器：${executor.mType}\nscanEnd...\n:总大小:$totalSize\n---总个数${list.size}"
                    )
                    cacheDb?.updateTime = System.currentTimeMillis()
                    CacheDbOption.insertCache(cacheDb)
                }

                override fun scanError() {
                    scanBean.status = JunkConstants.ScanStatus.ERROR
                    Log.d(TAG, "执行器：${executor.mType}\nscanError")
                    CacheDbOption.insertCache(cacheDb)
                }

                override fun scanIdle(appJunk: AppJunk) {
                    scanBean.status = JunkConstants.ScanStatus.SCAN_IDLE
                    scanBean.junk.appJunks?.add(appJunk)
                    cacheJunk.junkSize += appJunk.junkSize
                    Log.d(
                        TAG,
                        "执行器：${executor.mType}\nscanIdle....\nName:${appJunk.appName}\n扫描大小${appJunk.junkSize}\n扫描个数${
                            appJunk.junks?.size
                        }"
                    )
                    CacheDbOption.insertCache(cacheDb)
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "异常：$e")
        }
    }
}