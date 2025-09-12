package com.yandex.demeter.plugin

import com.yandex.demeter.compose.plugin.DemeterComposePlugin
import com.yandex.demeter.inject.plugin.DemeterInjectPlugin
import com.yandex.demeter.tracer.plugin.DemeterTracerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

open class DemeterPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.requireAndroidApp()

        target.extensions.create(DemeterExtension.name, DemeterExtension::class.java)

        target.plugins.apply(DemeterTracerPlugin::class.java)
        target.plugins.apply(DemeterInjectPlugin::class.java)
        target.plugins.apply(DemeterComposePlugin::class.java)

        target.afterEvaluate {
            target.validateDependencies()
        }
    }
}
