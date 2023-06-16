package com.example.componentialdemo.ui

import android.app.Application
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.core.LogisticsCenter.completion
import com.alibaba.android.arouter.launcher.ARouter


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ARouter.openLog();     // 打印日志
        ARouter.openDebug();   // 开启调试模式
        ARouter.init(this)
    }
}