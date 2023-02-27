package com.jack.android.gradle.method.trace.asm.runcatch;

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input

abstract class RunCatchingParameters : InstrumentationParameters {
    @get:Input
    abstract val including: ListProperty<String>
    @get:Input
    abstract val includingClass: MapProperty<String, List<String>>
}