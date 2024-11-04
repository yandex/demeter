package com.yandex.demeter.plugin

import com.android.build.api.instrumentation.ClassData
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.android.build.gradle.AppPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project

inline val ClassData.isKtIntrinsics: Boolean
    get() = className.endsWith("WhenMappings")

fun Project.beforeAndroidComponentVariants(callback: (VariantBuilder) -> Unit) {
    extensions.getByType(AndroidComponentsExtension::class.java).beforeVariants(callback = callback)
}

fun Project.onAndroidComponentVariants(callback: (Variant) -> Unit) {
    extensions.getByType(AndroidComponentsExtension::class.java).onVariants(callback = callback)
}

fun Project.requireAndroidApp() {
    if (!plugins.hasPlugin(AppPlugin::class.java)) {
        throw GradleException("Plugin must be applied only for 'com.android.application' module")
    }
}
