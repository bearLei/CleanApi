package com.mckj.api.init

import com.mckj.api.db.entity.CacheDb
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import kotlin.Throws
import io.reactivex.rxjava3.core.ObservableEmitter

/**
 * create by leix on 2022/4/28
 * desc:
 */
object Test {
    private fun a() {
        Observable.create<CacheDb> { }
    }
}