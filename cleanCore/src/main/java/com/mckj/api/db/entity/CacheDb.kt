package com.mckj.api.db.entity

import androidx.room.*
import com.mckj.api.db.ScanBeanConvert
import com.mckj.api.entity.ScanBean

/**
 *
create by leix on 2022/4/27
desc:
 */
@Entity(tableName = "cachDb")
@TypeConverters(ScanBeanConvert::class)
data class CacheDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val _id: Long? = 0,
    @ColumnInfo(name = "executorType") var executorType: Int = 0,
    @ColumnInfo(name = "updateTime") var updateTime: Long = 0L,

    @ColumnInfo(name = "scanBean")
    var scanBean: ScanBean? = null

)