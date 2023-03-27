package com.jack.android.gradle.method.trace.asm.methodtrace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor

abstract class MethodTraceAsmClassVisitorFactory : AsmClassVisitorFactory<MethodTraceParameters> {

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        // 1. When we find the class name (com.android.test.MainActivity), it means we should inject code into all the methods.
        // 2. When we find class with method (com.android.test.MainActivity#onCreate) it means we only wants to inject code into the method.
        val classData = classContext.currentClassData
        if (includingClass().any { it.contains(classData.className) }) {
            val includingMethods = includingMethods().getOrDefault(classData.className, emptyList())
            println("\t$classData.className trace method:${includingMethods}")
            return MethodTraceClassVisitor(
                classVisitor = nextClassVisitor,
                /* empty means the className */
                includingAllMethods = includingMethods.contains(""),
                includingMethods = includingMethods
            )
        }
        // Super class
        val superClassName = classData.superClasses.find { superClass ->
            val configureSuperClass = includingClass().find { it.contains(superClass) }
            configureSuperClass?.endsWith("*") == true
        }
        if (null != superClassName) {
            val includingMethods = includingMethods().getOrDefault("$superClassName*", emptyList())
            println("\t$classData.className trace method:${includingMethods}")
            return MethodTraceClassVisitor(
                classVisitor = nextClassVisitor,
                /* empty means the className */
                includingAllMethods = includingMethods.contains(""),
                includingMethods = includingMethods
            )
        }
        return nextClassVisitor
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        // Check current class
        if (includingClass().any { it.contains(classData.className) }) {
            return true
        }
        // Check super class
        if (classData.superClasses.any { superClass ->
                val configureSuperClass = includingClass().find { it.contains(superClass) }
                configureSuperClass?.endsWith("*") == true
            }
        ) {
            return true
        }
        return false
    }

    private fun includingClass(): MutableList<String> {
        val runCatchingParameters = parameters.get()
        return runCatchingParameters.including.getOrElse(emptyList())
    }

    private fun includingMethods(): MutableMap<String, List<String>> {
        val runCatchingParameters = parameters.get()
        return runCatchingParameters.includingMethods.getOrElse(emptyMap())
    }
}