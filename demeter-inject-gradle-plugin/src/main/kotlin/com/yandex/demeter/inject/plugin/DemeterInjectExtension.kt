package com.yandex.demeter.inject.plugin

import com.yandex.demeter.plugin.FeatureExtension
import org.gradle.api.Project

open class DemeterInjectExtension : FeatureExtension() {
    internal var debug: Boolean = false

    var includedClasses: List<String> = listOf()
    var excludedClasses: List<String> = listOf()

    override val extensionName = NAME

    override fun apply(project: Project) {
        project.configureExtension<DemeterInjectExtension> { extension ->
            extension.debug = debug
            extension.includedClasses = includedClasses
            extension.excludedClasses = excludedClasses
        }
    }

    companion object {
        const val NAME = "demeterInject"
    }
}
