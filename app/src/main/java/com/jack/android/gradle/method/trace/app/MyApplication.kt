package com.jack.android.gradle.method.trace.app

import android.app.Application
import com.jack.android.gradle.method.trace.runtime.MethodTrace

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MethodTrace.registerMethodAnalyzer(PlayerMethodAnalyzer())
    }
}