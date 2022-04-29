package com.mckj.api.client.impl

import androidx.lifecycle.LiveData
import com.mckj.api.client.JunkExecutor
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.entity.JunkInfo

/**
 *
create by leix on 2022/4/26
desc:
 */
interface IClientAbility {
    fun scan(executorType: Int, iScanCallBack: IScanCallBack)
    fun scan(executorType: Int):LiveData<CacheDb>?
    fun clean(list: MutableList<JunkInfo>, iCleanCallBack: ICleanCallBack)
    fun silentClean(list: MutableList<JunkInfo>)
    fun stop(executor: JunkExecutor)
}