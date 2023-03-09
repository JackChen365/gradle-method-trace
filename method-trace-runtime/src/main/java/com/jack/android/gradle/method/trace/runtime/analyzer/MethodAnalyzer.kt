package com.jack.android.gradle.method.trace.runtime.analyzer

interface MethodAnalyzer {
    fun onMethodEnter(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?
    )

    fun onMethodExit(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?,
        result: Any?,
        startTime: Long
    )

    fun onException(clazz: Class<*>,ref: Any?, methodName: String?, exception: Exception)
}