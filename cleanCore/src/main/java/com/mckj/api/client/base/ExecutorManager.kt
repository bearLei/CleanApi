package com.mckj.api.client.base

import com.mckj.api.client.JunkExecutor
import com.mckj.api.init.JunkInitializer

/**
 *
create by leix on 2022/4/29
desc:
 */
class ExecutorManager {

    companion object {
        const val TAG = "ScenesManager"

        private val INSTANCE by lazy { ExecutorManager() }

        fun getInstance(): ExecutorManager = INSTANCE
    }

    /**
     * 执行器集合
     * key-类型，
     * JunkExecutor 具体的执行器
     */
    private val mExecutorMap: MutableMap<Int, JunkExecutor> by lazy { mutableMapOf() }


    fun register(type: Int, junkExecutor: JunkExecutor) {
        mExecutorMap[type] = junkExecutor
    }

    fun unRegister(type: Int) {
        mExecutorMap.remove(type)
    }


    /**
     * 获取执行器
     * @param type 执行器类型
     */
    fun getExecutor(type: Int): JunkExecutor? {
        return mExecutorMap[type]
    }
}