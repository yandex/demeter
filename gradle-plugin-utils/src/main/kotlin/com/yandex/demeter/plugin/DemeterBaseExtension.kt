package com.yandex.demeter.plugin

import org.gradle.api.Project

abstract class FeatureExtension {

    abstract val extensionName: String

    abstract fun apply(project: Project)

    protected inline fun <reified T> Project.configureExtension(configure: (T) -> Unit = {}) {
        val extension = extensions.findByType(T::class.java)
            ?: extensions.create(extensionName, T::class.java)
        extension.apply(configure)
    }
}
