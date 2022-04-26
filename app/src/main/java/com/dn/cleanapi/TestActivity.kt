package com.dn.cleanapi

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mckj.api.client.JunkConstants
import com.mckj.api.client.base.JunkClientNew
import com.mckj.api.client.task.CleanCooperation
import com.mckj.api.client.task.JunkExecutorNew
import com.mckj.api.entity.AppJunk
import com.mckj.api.entity.ScanBean
import com.mckj.api.util.CleanCoreMod
import com.tbruyelle.rxpermissions3.RxPermissions

/**
 * @author leix
 * @version 1
 * @createTime 2022/1/20 14:36
 * @desc
 */
class TestActivity : AppCompatActivity() {
    private lateinit var mResult: TextView
    private var mScanBean: ScanBean? = null
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
            DataTransport.getInstance().put("junk_list",list)
            val intent = Intent(this, JunkDetailActivity::class.java)
            startActivity(intent)
        }
    }


    private fun subscribeUi() {
        JunkClientNew.instance.getHomeScanLiveData().observe(this) {
            mScanBean = it
            val status = when (it.status) {
                JunkConstants.ScanStatus.SCAN_IDLE -> "扫描中..."
                JunkConstants.ScanStatus.START -> "扫描开始"
                JunkConstants.ScanStatus.COMPLETE -> "扫描结束"
                JunkConstants.ScanStatus.ERROR -> "扫描错误"
                JunkConstants.ScanStatus.CLEAN -> "清理状态"
                else -> "默认状态"
            }
            val junk = it.junk
            val junkSize = junk?.junkSize
            mResult.text = "扫描状态：$status\n 扫描结果：${FileUtil.getFileSizeText(junkSize!!)}"
        }
    }

    /**
     * 首页扫描
     */
    private fun startScanHome() {
        val executor = CleanCooperation.getCacheExecutor()
        JunkClientNew.instance.scanByHome(executor)
    }


}