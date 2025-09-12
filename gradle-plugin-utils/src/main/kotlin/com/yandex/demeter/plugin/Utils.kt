package com.yandex.demeter.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project

inline val ClassData.isKtIntrinsics: Boolean
    get() = className.endsWith("WhenMappings")

val Project.android
    get() = extensions.getByType(ApplicationExtension::class.java)

fun Project.androidComponents(block: ApplicationAndroidComponentsExtension.() -> Unit) {
    extensions.getByType(ApplicationAndroidComponentsExtension::class.java).block()
}

fun Project.requireAndroidApp() {
    if (!plugins.hasPlugin(AppPlugin::class.java)) {
        throw GradleException("Plugin must be applied only for 'com.android.application' module")
    }
}
