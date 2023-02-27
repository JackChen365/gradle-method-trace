package com.jack.android.gradle.method.trace.runtime.exception

/**
 * Trace class or class method.
 * Method or class annotated with [RunCatching] will put the method body into try-catch
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RunCatching