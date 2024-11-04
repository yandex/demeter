import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.base)
}

android {
    namespace = "com.yandex.demeter.profiler.inject"

    viewBinding.enable = true
}

publishLib("profiler-inject-plugin")

dependencies {
    api(projects.demeterProfilerBase)
    api(projects.demeterProfilerUi)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)

    implementation(libs.androidx.constraintLayout)
}
