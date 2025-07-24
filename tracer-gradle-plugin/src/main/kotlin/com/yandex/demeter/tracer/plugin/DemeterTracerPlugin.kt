package com.yandex.demeter.tracer.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.yandex.demeter.plugin.DEMETER_FRAMES_COMPUTATION_MODE
import com.yandex.demeter.plugin.onAndroidComponentVariants
import com.yandex.demeter.plugin.requireAndroidApp
import com.yandex.demeter.tracer.plugin.asm.TracerClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterTracerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.requireAndroidApp()

        val extension = target.extensions.create(DemeterTracerExtension.NAME, DemeterTracerExtension::class.java)

        target.onAndroidComponentVariants { variant ->
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
