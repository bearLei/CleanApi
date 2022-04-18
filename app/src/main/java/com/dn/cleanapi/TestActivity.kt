package com.dn.cleanapi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * @author leix
 * @version 1
 * @createTime 2022/1/20 14:36
 * @desc
 */
class TestActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("leix","testActi")
        setContentView(R.layout.test_acty)
    }
}