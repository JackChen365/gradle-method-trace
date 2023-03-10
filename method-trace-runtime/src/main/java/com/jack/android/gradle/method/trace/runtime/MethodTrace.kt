package com.jack.android.gradle.method.trace.runtime

import com.jack.android.gradle.method.trace.runtime.analyzer.MethodAnalyzer

object MethodTrace {
    private val methodAnalyzers = mutableListOf<MethodAnalyzer>()

    @JvmStatic
    fun onMethodEnter(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?
    ) {
        methodAnalyzers.forEach { methodAnalyzer ->
            methodAnalyzer.onMethodEnter(identifier, clazz, ref, arguments, methodName)
        }
    }

    @JvmStatic
    fun onMethodExit(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?,
        result: Any?,
        startTime: Long
    ) {
        methodAnalyzers.forEach { methodAnalyzer ->
            methodAnalyzer.onMethodExit(
                identifier,
                clazz,
                ref,
                arguments,
                methodName,
                result,
                startTime
            )
        }
    }

    @JvmStatic
    fun onException(clazz: Class<*>, ref: Any?, methodName: String?, exception: Exception) {
        methodAnalyzers.forEach { methodAnalyzer ->
            methodAnalyzer.onException(clazz, ref, methodName, exception)
        }
    }

    fun registerMethodAnalyzer(methodAnalyzer: MethodAnalyzer) {
        if (!methodAnalyzers.contains(methodAnalyzer)) {
            methodAnalyzers.add(methodAnalyzer)
        }
    }

    fun unregisterMethodAnalyzer(methodAnalyzer: MethodAnalyzer) {
        methodAnalyzers.remove(methodAnalyzer)
    }

}