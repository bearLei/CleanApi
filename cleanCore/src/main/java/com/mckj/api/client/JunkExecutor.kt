package com.mckj.api.client

import android.util.Log
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.base.BaseTask
import com.mckj.api.entity.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author leix
 * @version 1
 * @createTime 2021/10/28 18:16
 * @desc 扫描任务执行者
 */
class JunkExecutor internal constructor(builder: Builder) {
    companion object {
        const val TAG = "ScanExecutor"
    }


    private val mScanTask: List<BaseTask>? = builder.scanTasks

    val mType: Int = builder.mType ?: -1

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

    class Builder {
        internal var scanTasks: List<BaseTask>? = null
        internal var mType: Int? = null

        fun type(type: Int) = apply {
            this.mType = type
        }

        fun task(tasks: List<BaseTask>) = apply {
            this.scanTasks = tasks
        }

        fun build(): JunkExecutor = JunkExecutor(this)
    }


}