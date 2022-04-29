package com.dn.cleanapi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckj.api.client.task.CleanCooperation
import com.mckj.api.client.impl.ICleanCallBack
import com.mckj.api.client.impl.IScanCallBack
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.JunkInfo
import com.mckj.api.util.ScopeHelper

/**
 *
create by leix on 2022/4/24
desc:
 */
class CleanSecondActivity : AppCompatActivity() {

    private lateinit var mScanCache: Button
    private lateinit var mCleanCache: Button
    private lateinit var mScanCacheResult: TextView
    private lateinit var mRecyclerView: RecyclerView



    var totalSize = 0L
    var junks = mutableListOf<AppJunk>()
    private val executor = CleanCooperation.getCacheExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        mScanCache = findViewById(R.id.scan_cache)
        mCleanCache = findViewById(R.id.clean_cache)
        mScanCacheResult = findViewById(R.id.result)
        mRecyclerView = findViewById(R.id.junk_list)

        mRecyclerView.layoutManager = LinearLayoutManager(this)


        mScanCache.setOnClickListener {
            scan()
        }
        mCleanCache.setOnClickListener {
            clean()
        }
    }


    private fun scan() {
        executor.scan(object : IScanCallBack {
            override fun scanStart() {

            }

            override fun scanEnd(totalSize: Long, list: MutableList<AppJunk>) {

            }

            override fun scanError() {

            }

            override fun scanIdle(appJunk: AppJunk) {
                totalSize += appJunk.junkSize
                junks.add(appJunk)
                mScanCacheResult.text = totalSize.toString()
            }
        })
    }

    private fun clean() {
        val details = mutableListOf<JunkInfo>()
        junks.forEach {
            details.addAll(it.junks!!)
        }

    }

}