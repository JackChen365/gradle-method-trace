package com.jack.android.gradle.method.trace.runtime

interface MethodAnalyzer {
    fun onMethodEnter(
        identifier: Int,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?
    )

    fun onMethodExit(
        identifier: Int,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?,
        result: Any?,
        startTime: Long
    )

    fun onException(ref: Any?, methodName: String?, exception: Exception)
}