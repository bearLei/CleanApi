package com.mckj.api.client.base

import android.util.Log
import androidx.lifecycle.LiveData
import com.mckj.api.client.impl.ICleanCallBack
import com.mckj.api.client.impl.IClientAbility
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.JunkExecutor
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.JunkInfo
import com.mckj.api.init.JunkInitializer
import com.mckj.api.manager.CacheDbOption
import com.mckj.api.util.FileUtils
import com.mckj.api.util.RFileUtils

class JunkClientNew : IClientAbility {

    companion object {
        const val TAG = "JunkClient"
        val instance: JunkClientNew by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { JunkClientNew() }
    }

    /**
     * @param executorType 执行器类型
     * @param iScanCallBack 扫描回调
     */
    override fun scan(executorType: Int, iScanCallBack: IScanCallBack) {
        JunkInitializer.getExecutor(executorType)?.scan(iScanCallBack)
    }

    /**
     * @param executorType 执行器类型
     */
    override fun scan(executorType: Int): LiveData<CacheDb>? {
        CacheDbOption.getCacheByType(executorType)?.let {
            Log.d(TAG, "数据库命中缓存对象")
            return it
        } ?: let {
            Log.d(TAG, "数据库未命中缓存对象:执行文件扫描")
            JunkInitializer.scan(executorType)
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
}