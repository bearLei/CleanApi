package com.mckj.api.client.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.task.IScanCallBack
import com.mckj.api.client.task.JunkExecutorNew
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.ScanBean

class JunkClientNew {

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
    fun scanByHome(executor: JunkExecutorNew) {
        val cacheJunk = CacheJunk(junkSize = 0L,appJunks = mutableListOf())
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


    fun scan(executor: JunkExecutorNew) {
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

    fun scan(executor: JunkExecutorNew, iScanCallBack: IScanCallBack) {
        executor.scan(iScanCallBack)
    }



    fun getHomeScanLiveData():MutableLiveData<ScanBean>{
        return mHomeLiveData
    }

}