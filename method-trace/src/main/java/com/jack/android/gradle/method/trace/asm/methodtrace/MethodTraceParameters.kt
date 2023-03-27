package com.jack.android.gradle.method.trace.asm.methodtrace;

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input

abstract class MethodTraceParameters : InstrumentationParameters {
    @get:Input
    abstract val including: ListProperty<String>
    @get:Input
    abstract val includingMethods: MapProperty<String, List<String>>
}