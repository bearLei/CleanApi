package com.mckj.api.init

import android.util.Log
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.CleanCooperation
import com.mckj.api.client.JunkExecutor
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.manager.CacheDbOption
import com.mckj.api.util.ScopeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
create by leix on 2022/4/27
desc:清理引擎初始化
 */
object JunkInitializer {

    const val TAG = "JunkInitializer"

    private val mExecutorMap = HashMap<Int, JunkExecutor>()

    fun initializeDb() {

        // TODO:  启动1个worker任务，处理定时清理功能
        register()
//        preScan()
    }

    /**
     * 注册执行器
     * 缓存执行器
     * 残留清理
     * 微信/QQ深度清理
     */
    private fun register() {
        CleanCooperation.getCacheExecutor().let {
            mExecutorMap.put(it.mType, it)
        }
        CleanCooperation.getResidualCleanExecutor().let {
            mExecutorMap.put(it.mType, it)
        }
        CleanCooperation.getQQDeepCleanExecutor().let {
            mExecutorMap.put(it.mType, it)
        }
        CleanCooperation.getWxDeepCleanExecutor().let {
            mExecutorMap.put(it.mType, it)
        }
    }

    /**
     * 获取执行器
     * @param type 执行器类型
     */
    fun getExecutor(type: Int): JunkExecutor? {
        return mExecutorMap[type]
    }

    /**
     *文件预扫描
     */
    private fun preScan() {
        ScopeHelper.launch {
            val preScanTask = getPreScanExecutor()
            if (preScanTask.isNullOrEmpty()) {
                Log.d(TAG, "preScanTask == null   return")
                return@launch
            }
            preScanTask.forEach {
                scan(it)
            }
        }
    }

    /**
     * @param executor
     * 文件扫描执行器
     */
    fun scan(type: Int) {
        val executor = mExecutorMap[type] ?: return
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

    /**
     * 预扫描的任务列表
     */
    private fun getPreScanExecutor(): List<Int> {
        val list = mutableListOf<Int>()
        list.add(JunkConstants.Session.APP_CACHE)
        return list
    }


}