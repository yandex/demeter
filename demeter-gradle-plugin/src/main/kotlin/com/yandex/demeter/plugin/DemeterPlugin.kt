package com.yandex.demeter.plugin

import com.yandex.demeter.compose.plugin.DemeterComposePlugin
import com.yandex.demeter.inject.plugin.DemeterInjectPlugin
import com.yandex.demeter.tracer.plugin.DemeterTracerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(DemeterExtension.name, DemeterExtension::class.java)

        target.beforeAndroidComponentVariants {
            extension.tracerConfig?.let { extension ->
                target.plugins.apply(DemeterTracerPlugin::class.java)
                extension.apply(target)
            }
            extension.injectConfig?.let { extension ->
                target.plugins.apply(DemeterInjectPlugin::class.java)
                extension.apply(target)
            }
            extension.composeConfig?.let { extension ->
                target.plugins.apply(DemeterComposePlugin::class.java)
                extension.apply(target)
            }
        }
    }
}
