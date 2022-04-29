package com.mckj.api.db

import androidx.room.*
import com.mckj.api.db.dao.CacheDbDao
import com.mckj.api.db.entity.CacheDb
import com.mckj.api.util.CleanCoreMod

/**
 * @author leix
 * @version 1
 * @createTime 2022/4/27 10:32
 * @desc 文件扫描缓存数据库
 */
@Database(entities = [CacheDb::class], version = 1, exportSchema = false)
abstract class CacheDatabase : RoomDatabase() {

    companion object {
        private const val DB_NAME = "cache.db"
        private val cacheDbInstance by lazy { createDatabase() }

        fun getInstance() = cacheDbInstance

        fun close() {

            cacheDbInstance.close()
        }

        private fun createDatabase(): CacheDatabase {
            return Room.databaseBuilder(
                CleanCoreMod.app,
                CacheDatabase::class.java, DB_NAME
            ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun getCacheDao(): CacheDbDao
}