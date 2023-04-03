package com.jack.android.gradle.method.trace

import com.jack.android.gradle.method.trace.asm.VisitScope
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Configuration object for [MethodTracePlugin].
 */
open class MethodTraceExtension(private val project: Project) {
    val runCatchingProperty: Property<RunCatching> = project.objects.property(RunCatching::class.java)
    val methodTraceProperty: Property<MethodTrace> = project.objects.property(MethodTrace::class.java)

    init {
        runCatchingProperty.convention(RunCatching(project))
        methodTraceProperty.convention(MethodTrace(project))
    }

    fun catch(action: Action<RunCatching>) {
        val runCatching = RunCatching(project)
        action.execute(runCatching)
        runCatchingProperty.set(runCatching)
    }

    fun trace(action: Action<MethodTrace>) {
        val methodTrace = MethodTrace(project)
        action.execute(methodTrace)
        methodTraceProperty.set(methodTrace)
    }

    class RunCatching(project: Project) {
        val isEnabled: Property<Boolean> = project.objects.property(Boolean::class.java)
        val scope: Property<Int> = project.objects.property(Int::class.java)
        val includes: ListProperty<String> = project.objects.listProperty(String::class.java)

        init {
            scope.convention(VisitScope.ALL)
            isEnabled.convention(true)
        }

        fun including(list: List<String>) {
            includes.addAll(list)
        }

        fun including(vararg list: String) {
            includes.addAll(list.toList())
        }
    }

    class MethodTrace(project: Project) {
        val isEnabled: Property<Boolean> = project.objects.property(Boolean::class.java)
        val scope: Property<Int> = project.objects.property(Int::class.java)
        val includes: ListProperty<String> = project.objects.listProperty(String::class.java)
        val includingSuperClass: ListProperty<String> = project.objects.listProperty(String::class.java)
        val excludes: ListProperty<String> = project.objects.listProperty(String::class.java)

        init {
            scope.convention(VisitScope.ALL)
            isEnabled.convention(true)
        }

        fun including(list: List<String>) {
            includes.addAll(list)
        }

        fun including(vararg list: String) {
            includes.addAll(list.toList())
        }

        fun includingSuperClass(list: List<String>) {
            includingSuperClass.addAll(list)
        }

        fun includingSuperClass(vararg list: String) {
            includingSuperClass.addAll(list.toList())
        }

        fun excluding(list: List<String>) {
            excludes.addAll(list)
        }

        fun excluding(vararg list: String) {
            excludes.addAll(list.toList())
        }

    }
}