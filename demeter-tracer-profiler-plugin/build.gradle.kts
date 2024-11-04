import extensions.includeTests
import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.base)
}

android {
    namespace = "com.yandex.demeter.profiler.tracer"

    viewBinding.enable = true

    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testOptions.unitTests.isReturnDefaultValues = true
}

includeTests()

publishLib("profiler-tracer-plugin")

dependencies {
    implementation(projects.demeterProfilerBase)
    implementation(projects.demeterProfilerUi)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)
    implementation(libs.androidx.collection)

    implementation(libs.androidx.constraintLayout)
    implementation(libs.fastadapter.core)
    implementation(libs.fastadapter.extensionExpandable)
    implementation(libs.fastScroll)
}
