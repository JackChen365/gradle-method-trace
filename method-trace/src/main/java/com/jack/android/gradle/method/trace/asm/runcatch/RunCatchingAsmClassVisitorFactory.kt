package com.jack.android.gradle.method.trace.asm.runcatch

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.jack.android.gradle.method.trace.asm.methodtrace.MethodTraceClassVisitor
import com.jack.android.gradle.method.trace.caching
import org.objectweb.asm.ClassVisitor

abstract class RunCatchingAsmClassVisitorFactory : AsmClassVisitorFactory<RunCatchingParameters> {

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        // 1. When we find the class name (com.android.test.MainActivity), it means we should inject code into all the methods.
        // 2. When we find class with method (com.android.test.MainActivity#onCreate) it means we only wants to inject code into the method.
        val includingMethods = includingClass().getOrDefault(classContext.currentClassData.className, emptyList())
        return RunCachingClassAdapter(
            classVisitor = nextClassVisitor,
            /* empty means the className */
            includingAllMethods = includingMethods.contains(""),
            includingMethods = includingMethods
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return including().caching.any {
            it.matches(classData.className)
        }
    }

    private fun including(): MutableList<String> {
        val runCatchingParameters = parameters.get()
        return runCatchingParameters.including.getOrElse(emptyList())
    }

    private fun includingClass(): MutableMap<String, List<String>> {
        val runCatchingParameters = parameters.get()
        return runCatchingParameters.includingClass.getOrElse(emptyMap())
    }
}