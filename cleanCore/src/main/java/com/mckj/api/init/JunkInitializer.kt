package com.mckj.api.init

import android.util.Log
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.CleanCooperation
import com.mckj.api.client.JunkExecutor
import com.mckj.api.client.base.ExecutorManager
import com.mckj.api.client.base.JunkClient
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

    fun initializeDb() {

        // TODO:  启动1个worker任务，处理定时清理功能
        register()
        preScan()
    }

    /**
     * 注册执行器
     * 缓存执行器
     * 残留清理
     * 微信/QQ深度清理
     */
    private fun register() {
        CleanCooperation.getCacheExecutor().let {
            ExecutorManager.getInstance().register(it.mType, it)
        }
        CleanCooperation.getResidualCleanExecutor().let {
            ExecutorManager.getInstance().register(it.mType, it)
        }
        CleanCooperation.getQQDeepCleanExecutor().let {
            ExecutorManager.getInstance().register(it.mType, it)
        }
        CleanCooperation.getWxDeepCleanExecutor().let {
            ExecutorManager.getInstance().register(it.mType, it)
        }
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
                JunkClient.instance.realScan(it)
            }
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