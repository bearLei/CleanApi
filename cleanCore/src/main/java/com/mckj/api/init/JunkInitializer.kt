package com.mckj.api.init

import android.util.Log
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.CleanCooperation
import com.mckj.api.client.task.JunkExecutorNew
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.manager.CacheDbOption
import com.mckj.api.util.ScopeHelper
import io.reactivex.rxjava3.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
create by leix on 2022/4/27
desc:清理引擎初始化
 */
object JunkInitializer {

    const val TAG = "JunkInitializer"

    fun initializeDb() {

        // TODO:  启动1个worker任务，处理定时清理功能
        preScan()
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
                preScan(it)
            }
        }
    }

    /**
     * @param executor
     * 文件扫描执行器
     */
    private suspend fun preScan(executor: JunkExecutorNew) {
        val cacheDb = CacheDb()
        val cacheJunk = CacheJunk(junkSize = 0L, appJunks = mutableListOf())
        val scanBean = ScanBean(junk = cacheJunk)
        cacheDb.executorType = executor.mType ?: -1
        cacheDb.scanBean = scanBean
        try {
            withContext(Dispatchers.IO) {
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
                        cacheDb.updateTime = System.currentTimeMillis()
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
            }
        } catch (e: Exception) {
            Log.d(TAG, "异常：$e")
        }
    }

    /**
     * 预扫描的任务列表
     */
    private fun getPreScanExecutor(): List<JunkExecutorNew> {
        val list = mutableListOf<JunkExecutorNew>()
        list.add(CleanCooperation.getCacheExecutor())
        return list
    }


}