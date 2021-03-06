package com.dn.cleanapi

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.base.HomeAutoScanner
import com.mckj.api.client.base.JunkClient
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.init.JunkInitializer
import com.mckj.api.util.ScopeHelper
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author leix
 * @version 1
 * @createTime 2022/1/20 14:36
 * @desc
 */
class TestActivity : AppCompatActivity() {
    private lateinit var mResult: TextView
    private var mScanBean: ScanBean? = null
    private var mStartTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_acty)
        mResult = findViewById(R.id.result)
        subscribeUi()
        findViewById<Button>(R.id.scan_cache).setOnClickListener {
            val rxPermissions = RxPermissions(this)
            rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { result ->
                    when {
                        result.granted -> {
                            startScanHome()
                        }
                        else -> {

                        }
                    }
                }
        }

        findViewById<Button>(R.id.forward_next).setOnClickListener {
            val list = mutableListOf<AppJunk>()
            mScanBean?.junk?.appJunks?.forEach {
                list.add(it)
            }
            DataTransport.getInstance().put("junk_list", list)
            val intent = Intent(this, JunkDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private val autoCleaner = HomeAutoScanner(JunkConstants.Session.APP_CACHE,3*1000L)
    private fun subscribeUi() {
        lifecycle.addObserver(autoCleaner)
        autoCleaner.cacheJunkLiveData.observe(this) {
            mScanBean = it
            val status = when (mScanBean!!.status) {
                JunkConstants.ScanStatus.SCAN_IDLE -> "?????????..."
                JunkConstants.ScanStatus.START -> "????????????"
                JunkConstants.ScanStatus.COMPLETE -> "????????????"
                JunkConstants.ScanStatus.ERROR -> "????????????"
                JunkConstants.ScanStatus.CLEAN -> "????????????"
                else -> "????????????"
            }
            val junk = mScanBean?.junk
            val junkSize = junk?.junkSize
            val cost = System.currentTimeMillis() - mStartTime
            mResult.text =
                "???????????????$status\n???????????????${FileUtil.getFileSizeText(junkSize!!)}\n???????????????$cost"
        }
        startScanHome()
    }

    /**
     * ????????????
     */
    private fun startScanHome() {
        mStartTime = System.currentTimeMillis()
        autoCleaner.scan()
    }


}