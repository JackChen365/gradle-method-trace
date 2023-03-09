package com.jack.android.gradle.method.trace.runtime.analyzer

/**
 * Bind this method to a target method.
 * Method annotate with [MethodException] will try to find the proper method from [MethodAnalyzer]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MethodException(val value: String)