package com.mckj.api.client.impl

import com.mckj.api.entity.JunkInfo


interface ICleanCallBack {
    fun cleanStart()
    fun cleanIdle(junkInfo: JunkInfo)
    fun cleanEnd(totalSize:Long,list: MutableList<JunkInfo>)
}