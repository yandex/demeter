import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.compose)
}

android {
    namespace = "com.yandex.demeter.profiler.compose"

    viewBinding.enable = true
}

publishLib("profiler-compose-plugin")

dependencies {
    api(projects.demeterProfilerBase)
    api(projects.demeterProfilerUi)

    implementation(libs.kotlin.reflect)

    implementation(libs.compose.animation)

    implementation(libs.fastadapter.core)
    implementation(libs.fastScroll)
}
