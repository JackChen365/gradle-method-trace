package com.jack.android.gradle.method.trace.runtime.trace

/**
 * Trace class or class method.
 * Method or class annotated with [Trace] will inject code into the method and let us trace the method.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Trace