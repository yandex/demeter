package com.yandex.demeter.coroutine.tracer.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.DslExtension
import com.yandex.demeter.plugin.DEMETER_FRAMES_COMPUTATION_MODE
import com.yandex.demeter.plugin.androidComponents
import com.yandex.demeter.plugin.requireAndroidApp
import com.yandex.demeter.coroutine.tracer.plugin.DemeterCoroutineTracerExtension.Companion.NAME
import com.yandex.demeter.coroutine.tracer.plugin.asm.CoroutineTracerClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterCoroutineTracerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.requireAndroidApp()
        target.extensions.create(NAME, DemeterCoroutineTracerProjectDslExtension::class.java)

        target.androidComponents {
            registerExtension(
                DslExtension.Builder(NAME)
                    .extendBuildTypeWith(DemeterCoroutineTracerBuildTypeDslExtension::class.java)
                    .build()
            ) { config ->
                target.objects.newInstance(
                    DemeterCoroutineTracerExtension::class.java,
                    config,
                    target
                )
            }

            try {
                onVariants { variant ->
                    val extension = variant.getExtension(DemeterCoroutineTracerExtension::class.java)
                        ?: return@onVariants

                    if (!extension.enabled.get()) {
                        return@onVariants
                    }

                    variant.instrumentation.transformClassesWith(
                        CoroutineTracerClassVisitorFactory::class.java,
                        InstrumentationScope.ALL
                    ) {
                        it.asmDebug.set(extension.debug)
                        it.includedClasses.set(extension.includedClasses)
                        it.excludedClasses.set(extension.excludedClasses)
                    }
                    variant.instrumentation.setAsmFramesComputationMode(
                        DEMETER_FRAMES_COMPUTATION_MODE
                    )
                }
            } catch (e: Exception) {
                target.logger.error("Failed to configure Demeter Coroutine Tracer plugin", e)
                throw e
            }
        }
    }
}
