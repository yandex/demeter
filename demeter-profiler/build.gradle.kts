import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.base).apply(true)
    alias(libs.plugins.module.android.compose).apply(false)
}

android.namespace = "com.yandex.demeter.profiler"

publishLib("profiler")

dependencies {
    api(projects.demeterProfilerBase)
    implementation(projects.demeterProfilerUi)
}
