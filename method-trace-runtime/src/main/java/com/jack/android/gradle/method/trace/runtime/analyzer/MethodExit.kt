package com.jack.android.gradle.method.trace.runtime.analyzer

/**
 * Bind this method to a target method.
 * Method annotate with [MethodExit] will try to find the proper method from [MethodAnalyzer]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MethodExit(val value: String)