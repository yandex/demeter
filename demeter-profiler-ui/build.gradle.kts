import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.base)
}

android {
    namespace = "com.yandex.demeter.profiler.ui"

    viewBinding.enable = true
}

publishLib("profiler-ui")

dependencies {
    api(projects.demeterCore)
    api(projects.demeterProfilerBase)

    implementation(libs.fastadapter.core)
    implementation(libs.fastadapter.extensionExpandable)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.material)
    implementation(libs.androidx.viewPager2)
    implementation(libs.androidx.collection)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)

    implementation(libs.fastScroll)
}
