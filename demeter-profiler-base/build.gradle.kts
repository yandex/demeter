import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.compose)
}

android.namespace = "com.yandex.demeter.profiler.base"

publishLib("profiler-base")

dependencies {
    api(projects.demeterCore)

    implementation(libs.kotlin.reflect)
}
