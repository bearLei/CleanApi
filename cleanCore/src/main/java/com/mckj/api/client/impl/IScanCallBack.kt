package com.mckj.api.client.impl

import com.mckj.api.entity.AppJunk


interface IScanCallBack {
    fun scanStart()
    fun scanEnd(totalSize:Long,list: MutableList<AppJunk>)
    fun scanError()
    fun scanIdle(appJunk: AppJunk)
}