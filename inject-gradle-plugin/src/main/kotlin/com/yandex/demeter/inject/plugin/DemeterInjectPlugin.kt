package com.yandex.demeter.inject.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.yandex.demeter.inject.plugin.asm.InjectClassVisitorFactory
import com.yandex.demeter.plugin.DEMETER_FRAMES_COMPUTATION_MODE
import com.yandex.demeter.plugin.onAndroidComponentVariants
import com.yandex.demeter.plugin.requireAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterInjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.requireAndroidApp()

        val extension = target.extensions.create(DemeterInjectExtension.NAME, DemeterInjectExtension::class.java)

        target.onAndroidComponentVariants { variant ->
            variant.instrumentation.transformClassesWith(
                InjectClassVisitorFactory::class.java,
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
