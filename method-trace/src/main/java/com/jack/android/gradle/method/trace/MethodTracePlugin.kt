package com.jack.android.gradle.method.trace

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.jack.android.gradle.method.trace.asm.VisitScope
import com.jack.android.gradle.method.trace.asm.methodtrace.MethodTraceAsmClassVisitorFactory
import com.jack.android.gradle.method.trace.asm.runcatch.RunCatchingAsmClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.kotlin.dsl.configure

class MethodTracePlugin : Plugin<Project> {
    companion object {
        const val EXTENSION_NAME = "methodTrace"
        private fun isAndroidProject(project: Project): Boolean {
            return null != project.plugins.findPlugin(AppPlugin::class.java) ||
                    null != project.plugins.findPlugin(LibraryPlugin::class.java)
        }
    }

    private lateinit var logger: Logger

    override fun apply(target: Project) {
        if (!isAndroidProject(target)) return
        logger = target.logger
        val methodTraceExtension = target.extensions.create(
            EXTENSION_NAME,
            MethodTraceExtension::class.java,
            target
        )
        configureTransformClass(target, methodTraceExtension)
    }

    private fun configureTransformClass(project: Project, extension: MethodTraceExtension) {
        project.plugins.forEach { plugin ->
            if (plugin is AppPlugin) {
                with(project.extensions) {
                    configure<ApplicationAndroidComponentsExtension> {
                        onVariants { variant ->
                            configureMethodTraceAsmClassVisitorFactory(extension, variant)
                            configureRunCachingAsmClassVisitorFactory(extension, variant)
                        }
                    }
                }
            }
            if (plugin is LibraryPlugin) {
                with(project.extensions) {
                    configure<LibraryAndroidComponentsExtension> {
                        onVariants { variant ->
                            configureMethodTraceAsmClassVisitorFactory(extension, variant)
                            configureRunCachingAsmClassVisitorFactory(extension, variant)
                        }
                    }
                }
            }
        }
    }

    private fun configureMethodTraceAsmClassVisitorFactory(
        extension: MethodTraceExtension,
        variant: Variant
    ) {
        val methodTrace = extension.methodTraceProperty.get()
        if (VisitScope.ALL == methodTrace.scope.get()) {
            variant.instrumentation.transformClassesWith(
                MethodTraceAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {
                it.including.set(methodTrace.includes)
            }
        } else {
            variant.instrumentation.transformClassesWith(
                MethodTraceAsmClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) {
                it.including.set(methodTrace.includes)
            }
        }
        variant.instrumentation.setAsmFramesComputationMode(
            FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
        )
    }

    private fun configureRunCachingAsmClassVisitorFactory(
        extension: MethodTraceExtension,
        variant: Variant
    ) {
        val runCaching = extension.runCatchingProperty.get()
        if (VisitScope.ALL == runCaching.scope.get()) {
            variant.instrumentation.transformClassesWith(
                RunCatchingAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {
                it.including.set(runCaching.includes)
            }
        } else {
            variant.instrumentation.transformClassesWith(
                RunCatchingAsmClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) {
                it.including.set(runCaching.includes)
            }
        }
        variant.instrumentation.setAsmFramesComputationMode(
            FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
        )
    }
}