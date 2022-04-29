package com.mckj.api.client.impl

import com.mckj.api.client.task.JunkExecutorNew
import com.mckj.api.entity.JunkInfo

/**
 *
create by leix on 2022/4/26
desc:
 */
interface IClientAbility {
    fun scanByHome(executor: JunkExecutorNew)
    fun scan(executorType: Int, iScanCallBack: IScanCallBack)
    fun scan(executorType: Int)
    fun scan(executor: JunkExecutorNew, iScanCallBack: IScanCallBack)
    fun scan(executor: JunkExecutorNew)
    fun clean(list: MutableList<JunkInfo>, iCleanCallBack: ICleanCallBack)
    fun silentClean(list: MutableList<JunkInfo>)
    fun stop(executor: JunkExecutorNew)
}