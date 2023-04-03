package com.jack.android.gradle.method.trace.asm.methodtrace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import extension.trace.asm.methodtrace.MethodTraceClassVisitor
import extension.trace.asm.methodtrace.MethodTraceParameters
import org.objectweb.asm.ClassVisitor

abstract class MethodTraceAsmClassVisitorFactory : AsmClassVisitorFactory<MethodTraceParameters> {
    companion object {
        private val cachingRegexMap: MutableMap<String, Regex> = mutableMapOf()
        private val List<String>.cachingRegex: List<Regex>
            get() {
                val regexList = mutableListOf<Regex>()
                forEach { patternString ->
                    var regex = cachingRegexMap[patternString]
                    if (null == regex) {
                        regex = patternString.replace("*", ".*").toRegex()
                        cachingRegexMap[patternString] = regex
                    }
                    regexList.add(regex)
                }
                return regexList
            }
    }

    private val includingClass: List<Regex> by lazy { includingClass() }
    private val excludingClass: List<Regex> by lazy { excludingClass() }
    private val includingMethods: Map<String, List<String>> by lazy { includingMethods() }
    private val includingSuperClass: List<Regex> by lazy { includingSuperClass() }

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        // 1. When we find the class name (com.android.test.MainActivity), it means we should inject code into all the methods.
        // 2. When we find class with method (com.android.test.MainActivity#onCreate) it means we only wants to inject code into the method.
        val classData = classContext.currentClassData
        if (includingClass.any { it.matches(classData.className) }) {
            val includingMethods = includingMethods.getOrDefault(classData.className, emptyList())
            println("\t$classData.className trace method:${includingMethods}")
            return MethodTraceClassVisitor(
                classVisitor = nextClassVisitor,
                /* empty means the className */
                includingAllMethods = includingMethods.contains(""),
                includingMethods = includingMethods
            )
        }
        // 3. When we find class from includingSuperClass. It means any class with the specific super class will be trace.
        val superClassName = classData.superClasses.find { superClass ->
            includingClass.any { it.matches(superClass) }
        }
        if (null != superClassName) {
            val includingMethods = includingMethods.getOrDefault("$superClassName", emptyList())
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
        if (excludingClass.none { it.matches(classData.className) } &&
            includingClass.any { it.matches(classData.className) }) {
            return true
        }
        // Check super class
        return classData.superClasses.any { superClass ->
            includingSuperClass.any { it.matches(superClass) }
        }
    }

    private fun includingClass(): List<Regex> {
        val methodTraceParameters = parameters.get()
        val including = methodTraceParameters.including.getOrElse(emptyList())
        return including.cachingRegex
    }

    private fun includingSuperClass(): List<Regex> {
        val methodTraceParameters = parameters.get()
        val includingSuperClass = methodTraceParameters.includingSuperClass.getOrElse(emptyList())
        return includingSuperClass.cachingRegex
    }

    private fun excludingClass(): List<Regex> {
        val methodTraceParameters = parameters.get()
        val excluding = methodTraceParameters.excluding.getOrElse(emptyList())
        return excluding.cachingRegex
    }

    private fun includingMethods(): MutableMap<String, List<String>> {
        val methodTraceParameters = parameters.get()
        return methodTraceParameters.includingMethods.getOrElse(emptyMap())
    }
}