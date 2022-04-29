package com.mckj.api.init

import android.os.Message
import android.util.Log
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.client.task.CleanCooperation
import com.mckj.api.client.task.JunkExecutorNew
import com.mckj.api.db.CacheDatabase
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.CacheJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.util.ScopeHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Handler
import java.util.logging.LogRecord

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


    private val mDbPool = mutableListOf<CacheDb>()


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
        var emitter: ObservableEmitter<CacheDb>? = null
        val observable = Observable.create<CacheDb> {
            emitter = it
        }
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
//                Log.d(TAG,"接收到发射：$cacheDb")
                CacheDatabase.getInstance().getCacheDao().insert(cacheDb)
            }
        try {
            withContext(Dispatchers.IO) {
                executor.scan(object : IScanCallBack {
                    override fun scanStart() {
                        Log.d(
                            TAG,
                            "执行器：${executor.mType}\nscanStart...执行线程${Thread.currentThread().name}"
                        )
                        scanBean.status = JunkConstants.ScanStatus.START
                        CacheDatabase.getInstance().getCacheDao().insert(cacheDb)
                    }

                    override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {
                        scanBean.status = JunkConstants.ScanStatus.COMPLETE
                        Log.d(
                            TAG,
                            "执行器：${executor.mType}\nscanEnd...\n:总大小:$totalSize\n---总个数${list.size}"
                        )
                        cacheDb.updateTime = System.currentTimeMillis()

                        CacheDatabase.getInstance().getCacheDao().insert(cacheDb)
                    }

                    override fun scanError() {
                        scanBean.status = JunkConstants.ScanStatus.ERROR
                        Log.d(TAG, "执行器：${executor.mType}\nscanError")
                        CacheDatabase.getInstance().getCacheDao().insert(cacheDb)
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
                        CacheDatabase.getInstance().getCacheDao().insert(cacheDb)
                    }
                })
            }
        } catch (e: Exception) {
            Log.d(TAG, "异常：$e")
        }
    }


    private fun getObservable(): Observable<CacheDb> {
        return Observable.create<CacheDb> {

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