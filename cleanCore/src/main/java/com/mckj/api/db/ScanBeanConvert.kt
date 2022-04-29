package com.mckj.api.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mckj.api.entity.ScanBean

/**
 *
create by leix on 2022/4/27
desc:
 */
class ScanBeanConvert {
    @TypeConverter
    fun revert(jsonString: String): ScanBean? {
        try {
            val type = object : TypeToken<ScanBean>() {}.type
            return Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun converter(bean: ScanBean): String {
        return Gson().toJson(bean)
    }
}