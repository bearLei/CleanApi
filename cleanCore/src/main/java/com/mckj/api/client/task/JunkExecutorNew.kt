package com.mckj.api.client.task

import android.util.Log
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.base.JunkClientNew
import com.mckj.api.client.impl.ICleanCallBack
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.base.BaseTask
import com.mckj.api.entity.*
import com.mckj.api.util.FileUtils
import com.mckj.api.util.RFileUtils
import com.mckj.api.util.ScopeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author leix
 * @version 1
 * @createTime 2021/10/28 18:16
 * @desc 扫描任务执行者
 */
class JunkExecutorNew internal constructor(builder: Builder) {
    companion object {
        const val TAG = "ScanExecutor"
    }


    private val mScanTask: List<BaseTask>? = builder.scanTasks

    val mType: Int? = builder.mType

    /**
     * 是否允许操作
     */
    private val mOptEnable = AtomicBoolean(true)

    /**
     * 是否执行中
     */
    private val mRunning = AtomicBoolean(false)


    /**
     * @param iScanCallBack 扫描回调
     * 扫描
     */
    fun scan(iScanCallBack: IScanCallBack) {

        if (mScanTask.isNullOrEmpty()) {
            Log.d(TAG, "scanTask  must not be empty")
            iScanCallBack.scanError()
            return
        }
        if (mRunning.get()) {
            iScanCallBack.scanError()
            Log.d(TAG, "scanTask  has bean started")
            return
        }
        var totalSize = 0L
        val junks = mutableListOf<AppJunk>()
        iScanCallBack.scanStart()
        Log.d(TAG, "scanStart!!")
        try {
            mScanTask.forEach {
                if (mOptEnable.get()) {
                    mRunning.set(true)
                    it.scan { appJunk ->
                        totalSize += appJunk.junkSize
                        junks.add(appJunk)
                        Log.d(TAG, "scanning...")
                        iScanCallBack.scanIdle(appJunk)
                    }
                }
            }
        } catch (e: Exception) {
            iScanCallBack.scanError()
            Log.d(TAG, "scanError!!")
            mOptEnable.set(false)
            Log.d(TAG, "scanTask  error:$e")
        } finally {
            Log.d(TAG, "scanEnd:scanSize:$totalSize")
            iScanCallBack.scanEnd(totalSize, junks)
        }
        mRunning.set(false)

    }

    /**
     * 移除垃圾
     */
    suspend fun clean(junks: MutableList<JunkInfo>, iCleanCallBack: ICleanCallBack) {
        withContext(Dispatchers.IO) {
            var removeSizeTotal = 0L
            val removeJunks = mutableListOf<JunkInfo>()
            junks.iterator().run {
                iCleanCallBack.cleanStart()
                while (hasNext()) {
                    val next = next()
                    if (!delete(next)) {
                        continue
                    }
                    removeSizeTotal += next.junkSize
                    removeJunks.add(next)
                    Log.d(TAG, "清理：${next.path}---大小：${next.junkSize}")
                    if (isHomeCacheExecutor()) {
                        Log.d(TAG, "首页缓存执行器命中，更新首页状态")
                        notifyHomeCache(next)
                    }
                    iCleanCallBack.cleanIdle(next)
                }
                iCleanCallBack.cleanEnd(removeSizeTotal, removeJunks)
            }
        }
    }


    private fun delete(junkInfo: JunkInfo): Boolean {
        junkInfo.uri?.let {
            return RFileUtils.deleteFile(it)
        } ?: let {
            return FileUtils.delete(junkInfo.path)
        }
    }

    /**
     * 是否是首页执行器
     */
    private fun isHomeCacheExecutor(): Boolean {
        if (mType == null) {
            return false
        }
        return mType == JunkConstants.Session.APP_CACHE
    }

    private fun notifyHomeCache(junkInfo: JunkInfo) {
        val homeValue = JunkClientNew.instance.getHomeScanLiveData().value
        homeValue?.junk?.let {
            it.appJunks?.forEach { appJunk ->
                val iterator = appJunk.junks?.iterator()
                while (iterator?.hasNext() == true) {
                    val next = iterator.next()
                    if (next.path == junkInfo.path) {
                        iterator.remove()//移除颗粒
                        appJunk.junkSize = appJunk.junkSize - junkInfo.junkSize//app统计颗粒减少
                        it.junkSize = it.junkSize - junkInfo.junkSize//总扫描大小减少
                    }
                }
            }
        }
//        val scanBean = ScanBean()
//        scanBean.status = JunkConstants.ScanStatus.CLEAN
//        scanBean.junk = homeValue?.junk
//        JunkClientNew.instance.getHomeScanLiveData().postValue(scanBean)
    }


    class Builder {
        internal var scanTasks: List<BaseTask>? = null
        internal var mType: Int? = null

        fun type(type: Int) = apply {
            this.mType = type
        }

        fun task(tasks: List<BaseTask>) = apply {
            this.scanTasks = tasks
        }

        fun build(): JunkExecutorNew = JunkExecutorNew(this)
    }


}