package com.mckj.api.client.base

import android.util.Log
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
        realScan(executorType, iScanCallBack)
    }

    /**
     * @param executorType 执行器类型
     * @return
     */
    override fun scan(executorType: Int) {
        realScan(executorType)
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
                val cacheJunkIterator = cacheJunk.appJunks?.iterator()
                while (cacheJunkIterator?.hasNext() == true) {
                    val appJunk = cacheJunkIterator.next()
                    val iterator = appJunk.junks?.iterator()
                    while (iterator?.hasNext() == true) {
                        val next = iterator.next()
                        if (junks.contains(next)) {
                            appJunk.junkSize = appJunk.junkSize - next.junkSize
                            cacheJunk.junkSize = cacheJunk.junkSize - next.junkSize
                            iterator.remove()
//                            if (appJunk.junks.isNullOrEmpty()) {
//                                cacheJunkIterator.remove()
//                            }
                        }
                    }
                }
            }
            CacheDbOption.insertCache(it)
        }
    }


    /**
     *真实扫描
     * @param type 扫描的执行器类型
     * @param iScanCallBack 扫描回调
     */
    fun realScan(type: Int, iScanCallBack: IScanCallBack? = null) {
        val executor = ExecutorManager.getInstance().getExecutor(type) ?: return
        var cacheDb: CacheDb? = null
        CacheDbOption.getCacheLiveDataByType(type)?.let {
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
                    scanBean.status = JunkConstants.ScanStatus.START
                    Log.d(
                        TAG,
                        "执行器：${executor.mType}\nscanStart...执行线程${Thread.currentThread().name}"
                    )
                    CacheDbOption.insertCache(cacheDb)
                    iScanCallBack?.scanStart()
                }

                override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {
                    iScanCallBack?.scanEnd(totalSize, list)
                    scanBean.status = JunkConstants.ScanStatus.COMPLETE
                    Log.d(
                        TAG,
                        "执行器：${executor.mType}\nscanEnd...\n:总大小:$totalSize\n---总个数${list.size}"
                    )
                    cacheDb?.updateTime = System.currentTimeMillis()
                    CacheDbOption.insertCache(cacheDb)
                }

                override fun scanError() {
                    iScanCallBack?.scanError()
                    scanBean.status = JunkConstants.ScanStatus.ERROR
                    Log.d(TAG, "执行器：${executor.mType}\nscanError")
                    CacheDbOption.insertCache(cacheDb)
                }

                override fun scanIdle(appJunk: AppJunk) {
                    iScanCallBack?.scanIdle(appJunk)
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

    fun scanBackground(type: Int) {
        val executor = ExecutorManager.getInstance().getExecutor(type) ?: return
        try {
            executor.scan(object : IScanCallBack {
                override fun scanStart() {

                }

                override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {
                    CacheDbOption.getCacheLiveDataByType(type)?.let {
                        it.value?.apply {
                            this.updateTime = System.currentTimeMillis()
                            this.scanBean?.junk?.junkSize = totalSize
                            this.scanBean?.junk?.appJunks = list
                            CacheDbOption.insertCache(this)
                            Log.d(TAG, "后台自动扫描：\n大小：$totalSize")
                        }
                    }
                }

                override fun scanError() {

                }

                override fun scanIdle(appJunk: AppJunk) {

                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "后台自动扫描失败：$e")
        }
    }

}