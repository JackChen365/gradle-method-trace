package com.jack.android.gradle.method.trace.asm.methodtrace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.jack.android.gradle.method.trace.caching
import org.objectweb.asm.ClassVisitor

abstract class MethodTraceAsmClassVisitorFactory : AsmClassVisitorFactory<MethodTraceParameters> {

    private val includingClass = mutableMapOf<String, List<String>>()

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        // 1. When we find the class name (com.android.test.MainActivity), it means we should inject code into all the methods.
        // 2. When we find class with method (com.android.test.MainActivity#onCreate) it means we only wants to inject code into the method.
        val className = classContext.currentClassData.className
        val includingMethods = includingClass.getOrDefault(className, emptyList())
        println("\t$className trace method:${includingMethods}")
        return MethodTraceClassVisitor(
            classVisitor = nextClassVisitor,
            /* empty means the className */
            includingAllMethods = includingMethods.contains(""),
            includingMethods = includingMethods
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        ensureInstrumentableClassMethods()
        return including().caching.any {
            it.matches(classData.className)
        }
    }

    private fun ensureInstrumentableClassMethods() {
        if (includingClass.isEmpty()) {
            including().groupBy {
                it.substringBefore("#")
            }.forEach { (className, methods) ->
                includingClass[className] = methods.map { it.substringAfter("#") }
            }
        }
    }

    private fun including(): MutableList<String> {
        val runCatchingParameters = parameters.get()
        return runCatchingParameters.including.getOrElse(emptyList())
    }
}