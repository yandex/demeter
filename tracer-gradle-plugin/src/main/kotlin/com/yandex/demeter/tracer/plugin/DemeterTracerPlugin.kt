package com.yandex.demeter.tracer.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.DslExtension
import com.yandex.demeter.plugin.DEMETER_FRAMES_COMPUTATION_MODE
import com.yandex.demeter.plugin.androidComponents
import com.yandex.demeter.plugin.requireAndroidApp
import com.yandex.demeter.tracer.plugin.DemeterTracerExtension.Companion.NAME
import com.yandex.demeter.tracer.plugin.asm.TracerClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterTracerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.requireAndroidApp()
        target.extensions.create(NAME, DemeterTracerProjectDslExtension::class.java)

        target.androidComponents {
            registerExtension(
                DslExtension.Builder(NAME)
                    .extendBuildTypeWith(DemeterTracerBuildTypeDslExtension::class.java)
                    .build()
            ) { config ->
                target.objects.newInstance(
                    DemeterTracerExtension::class.java,
                    config,
                    target
                )
            }

            onVariants { variant ->
                val extension = variant.getExtension(DemeterTracerExtension::class.java) ?: run {
                    return@onVariants
                }

                if (!extension.enabled.get()) {
                    return@onVariants
                }

                variant.instrumentation.transformClassesWith(
                    TracerClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) {
                    it.asmDebug.set(extension.debug)
                    it.includedClasses.set(extension.includedClasses)
                    it.excludedClasses.set(extension.excludedClasses)
                }
                variant.instrumentation.setAsmFramesComputationMode(DEMETER_FRAMES_COMPUTATION_MODE)
            }
        }
    }
}
