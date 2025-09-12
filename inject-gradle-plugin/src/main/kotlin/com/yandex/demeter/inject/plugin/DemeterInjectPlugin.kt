package com.yandex.demeter.inject.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.DslExtension
import com.yandex.demeter.inject.plugin.DemeterInjectExtension.Companion.NAME
import com.yandex.demeter.inject.plugin.asm.InjectClassVisitorFactory
import com.yandex.demeter.plugin.DEMETER_FRAMES_COMPUTATION_MODE
import com.yandex.demeter.plugin.androidComponents
import com.yandex.demeter.plugin.requireAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterInjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.requireAndroidApp()
        target.extensions.create(NAME, DemeterInjectProjectDslExtension::class.java)

        target.androidComponents {
            registerExtension(
                DslExtension.Builder(NAME)
                    .extendBuildTypeWith(DemeterInjectBuildTypeDslExtension::class.java)
                    .build()
            ) { config ->
                target.objects.newInstance(
                    DemeterInjectExtension::class.java,
                    config,
                    target
                )
            }

            onVariants { variant ->
                val extension = variant.getExtension(DemeterInjectExtension::class.java) ?: run {
                    return@onVariants
                }

                if (!extension.enabled.get()) {
                    return@onVariants
                }

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
}
