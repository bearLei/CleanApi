package com.mckj.api.client.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.impl.ICleanCallBack
import com.mckj.api.client.impl.IClientAbility
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.JunkExecutorNew
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.JunkInfo
import com.mckj.api.entity.ScanBean
import com.mckj.api.manager.CacheDbOption
import com.mckj.api.util.FileUtils
import com.mckj.api.util.RFileUtils

class JunkClientNew : IClientAbility {

    companion object {
        const val TAG = "JunkClient"
        val instance: JunkClientNew by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { JunkClientNew() }
    }

    /**
     * 首页扫描数据
     */
    private val mHomeLiveData = MutableLiveData<ScanBean>()


    /**
     * @param executor 执行器
     * 首页扫描
     */
    override fun scanByHome(executor: JunkExecutorNew) {
        val cacheJunk = CacheJunk(junkSize = 0L, appJunks = mutableListOf())
        val scanBean = ScanBean(junk = cacheJunk)
        executor.scan(object : IScanCallBack {
            override fun scanStart() {
                scanBean.status = JunkConstants.ScanStatus.START
                mHomeLiveData.postValue(scanBean)
            }

            override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {
                scanBean.status = JunkConstants.ScanStatus.COMPLETE
                mHomeLiveData.postValue(scanBean)
            }

            override fun scanError() {
                scanBean.status = JunkConstants.ScanStatus.ERROR
                mHomeLiveData.postValue(scanBean)
            }

            override fun scanIdle(appJunk: AppJunk) {
                scanBean.status = JunkConstants.ScanStatus.SCAN_IDLE
                cacheJunk.junkSize += appJunk.junkSize
                cacheJunk.appJunks?.add(appJunk)
                mHomeLiveData.postValue(scanBean)
            }
        })
    }

    override fun scan(executorType: Int, iScanCallBack: IScanCallBack) {

    }

    override fun scan(executorType: Int) {

    }

    /**
     * 普通执行器扫描
     */
    override fun scan(executor: JunkExecutorNew) {
        executor.scan(object : IScanCallBack {
            override fun scanStart() {

            }

            override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {

            }


            override fun scanError() {

            }

            override fun scanIdle(appJunk: AppJunk) {

            }
        })
    }

    /**
     * @param iScanCallBack 扫描回调
     */
    override fun scan(executor: JunkExecutorNew, iScanCallBack: IScanCallBack) {
        executor.scan(iScanCallBack)
    }

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
                notifyHomeCache(next)
                iCleanCallBack.cleanIdle(next)
            }
            Log.d(TAG, "清理结束")
            iCleanCallBack.cleanEnd(removeSizeTotal, removeJunks)
        }
    }

    override fun silentClean(list: MutableList<JunkInfo>) {

    }

    override fun stop(executor: JunkExecutorNew) {

    }


    private fun delete(junkInfo: JunkInfo): Boolean {
        junkInfo.uri?.let {
            return RFileUtils.deleteFile(it)
        } ?: let {
            return FileUtils.delete(junkInfo.path)
        }
    }

    private fun notifyHomeCache(junkInfo: JunkInfo) {
//        CacheDbOption.getCacheByType()
    }

    fun getHomeScanLiveData(): MutableLiveData<ScanBean> {
        return mHomeLiveData
    }


}