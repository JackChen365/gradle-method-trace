package com.jack.android.gradle.method.trace.app

import com.jack.android.gradle.method.trace.runtime.MethodAnalyzer

class PlayerMethodAnalyzer: MethodAnalyzer {
    override fun onMethodEnter(identifier: Int, ref: Any?, arguments: Array<Any?>?, methodName: String?) {
    }

    override fun onMethodExit(
        identifier: Int,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?,
        result: Any?,
        startTime: Long
    ) {
    }

    override fun onException(ref: Any?, methodName: String?, exception: Exception) {
        TODO("Not yet implemented")
    }
}