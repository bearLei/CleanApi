package com.mckj.api.util

import java.io.File

/**
 * @author leix
 * @version 1
 * @createTime 2021/8/7 10:12
 * @desc
 */
object DbUtil {
    const val TAG = "DbMonitor"

    //解密文件存储的临时路径
    fun getDbPath(): String {
        val packDir = FileUtils.getAppPath("")
        val sqlDir = "$packDir/sql"
        val file = File(sqlDir)
        if (!file.exists()) {
            file.mkdirs()
        }
        return "$sqlDir/cleanup.db"
    }

    /**
     * 获取1个临时的下载地址
     */
    fun getDbDownLoadPath(): String {
        val packDir = FileUtils.getAppCachePath("")
        val file = File(packDir)
        if (!file.exists()) {
            file.mkdirs()
        }
        return "$packDir/cleanup.db"
    }

}