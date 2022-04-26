package com.mckj.api.client.task

import com.mckj.api.entity.JunkInfo


interface ICleanCallBack {
    fun cleanStart()
    fun cleanIdle(junkInfo: JunkInfo)
    fun cleanEnd(totalSize:Long,list: MutableList<JunkInfo>)
}