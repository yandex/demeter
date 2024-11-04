package com.yandex.demeter.compose.plugin

import com.yandex.demeter.plugin.FeatureExtension
import org.gradle.api.Project

open class DemeterComposeExtension : FeatureExtension() {

    override val extensionName = NAME

    override fun apply(project: Project) {
        project.configureExtension<DemeterComposeExtension>()
    }

    companion object {
        const val NAME = "demeterCompose"
    }
}
