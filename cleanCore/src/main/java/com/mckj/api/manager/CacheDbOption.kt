package com.mckj.api.manager

import androidx.lifecycle.LiveData
import com.mckj.api.db.CacheDatabase
import com.mckj.api.db.entity.CacheDb

/**
 *  author : leix
 *  date : 2022/4/29 8:26
 *  description :扫描缓存数据库操作
 */
object CacheDbOption {
    private val mCacheDbDao by lazy { CacheDatabase.getInstance().getCacheDao() }

    /**
     * 插入扫描缓存
     * @param cacheDb 扫描对象
     */
    fun insertCache(cacheDb: CacheDb) {
        mCacheDbDao.insert(cacheDb)
    }

    /**
     * 根据执行器类型获取扫描缓存对象
     * @param type 执行器类型
     * @return 扫描对象，LiveData包裹，可实时更新
     */
    fun getCacheByType(type: Int): LiveData<CacheDb>? {
        return mCacheDbDao.getCacheByType(type)
    }

    fun getAllCache():List<CacheDb>?{
        return mCacheDbDao.getAllCacheList()
    }


    /**
     * 删除全部缓存对象
     */
    fun deleteAll(){
        mCacheDbDao.deleteAll()
    }

    /**
     * 删除缓存对象
     * @param type 执行器对象
     */
    fun deleteByType(type: Int){
        mCacheDbDao.deleteByType(type)
    }
}