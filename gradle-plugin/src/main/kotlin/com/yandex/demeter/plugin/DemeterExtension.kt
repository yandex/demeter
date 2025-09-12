package com.yandex.demeter.plugin

import com.android.build.api.dsl.ApplicationBuildType
import com.yandex.demeter.compose.plugin.DemeterComposeBuildTypeDslExtension
import com.yandex.demeter.compose.plugin.DemeterComposeProjectDslExtension
import com.yandex.demeter.inject.plugin.DemeterInjectBuildTypeDslExtension
import com.yandex.demeter.inject.plugin.DemeterInjectProjectDslExtension
import com.yandex.demeter.tracer.plugin.DemeterTracerBuildTypeDslExtension
import com.yandex.demeter.tracer.plugin.DemeterTracerProjectDslExtension
import org.gradle.api.Project

interface DemeterExtension {

    fun Project.tracer(configure: DemeterTracerProjectDslExtension.() -> Unit = {}) {
        extensions.getByType(DemeterTracerProjectDslExtension::class.java).apply {
            enabled = true
        }.configure()
    }

    fun ApplicationBuildType.tracer(configure: DemeterTracerBuildTypeDslExtension.() -> Unit = {}) {
        extensions.getByType(DemeterTracerBuildTypeDslExtension::class.java).apply {
            enabled = true
        }.configure()
    }

    fun Project.inject(configure: DemeterInjectProjectDslExtension.() -> Unit = {}) {
        extensions.getByType(DemeterInjectProjectDslExtension::class.java).apply {
            enabled = true
        }.configure()
    }

    fun ApplicationBuildType.inject(configure: DemeterInjectBuildTypeDslExtension.() -> Unit = {}) {
        extensions.getByType(DemeterInjectBuildTypeDslExtension::class.java).apply {
            enabled = true
        }.configure()
    }

    fun Project.compose(configure: DemeterComposeProjectDslExtension.() -> Unit = {}) {
        extensions.getByType(DemeterComposeProjectDslExtension::class.java).apply {
            enabled = true
        }.configure()
    }

    fun ApplicationBuildType.compose(configure: DemeterComposeBuildTypeDslExtension.() -> Unit = {}) {
        extensions.getByType(DemeterComposeBuildTypeDslExtension::class.java).apply {
            enabled = true
        }.configure()
    }

    companion object {
        val name = "demeter"
    }
}
