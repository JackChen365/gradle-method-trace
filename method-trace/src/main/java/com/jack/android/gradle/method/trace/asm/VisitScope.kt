package com.jack.android.gradle.method.trace.asm

object VisitScope {
    /**
     * Instrument the classes of the current project only.
     *
     * Libraries that this project depends on will not be instrumented.
     */
    const val PROJECT = 0

    /**
     * Instrument the classes of the current project and its library dependencies.
     *
     * This can't be applied to library projects, as instrumenting library dependencies will have no
     * effect on library consumers.
     */
    const val ALL = 1
}