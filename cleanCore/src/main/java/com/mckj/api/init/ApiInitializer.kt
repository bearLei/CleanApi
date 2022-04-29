package com.mckj.api.init

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.mckj.api.util.CleanCoreMod

/**
 *
 *create by leix on 2022/4/24
 * desc:启动初始化
 */
class ApiInitializer : Initializer<Boolean> {
    override fun create(context: Context): Boolean {
        CleanCoreMod.app = context
        JunkInitializer.initializeDb()
        return true
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}