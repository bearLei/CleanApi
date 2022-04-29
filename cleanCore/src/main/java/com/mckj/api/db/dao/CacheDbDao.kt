package com.mckj.api.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mckj.api.db.entity.CacheDb

/**
 * @author leix
 * @version 1
 * @createTime 2021/8/4 10:32
 * @desc
 */
@Dao
interface CacheDbDao {

    @Query("SELECT * FROM cachDb WHERE executorType = :type")
    fun getCacheByType(type:Int): LiveData<CacheDb>?

    @Query("SELECT * FROM cachDb")
    fun getAllCacheList(): List<CacheDb>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: CacheDb)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(bean: CacheDb)

    @Query("DELETE FROM cachDb")
    suspend fun deleteAll()

    @Query("DELETE FROM cachDb where executorType = :type")
    fun deleteByType(type: Long)
}