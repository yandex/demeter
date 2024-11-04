plugins {
    alias(libs.plugins.module.android.base)
}

android {
    namespace = "com.yandex.demeter.benchmark"

    defaultConfig {
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }

    testBuildType = "release"
    buildTypes {
        release {
            isDefault = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "benchmark-proguard-rules.pro"
            )
        }
    }
}

dependencies {
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.benchmark)

    androidTestImplementation(projects.demeterCore) // benchmarkable
    androidTestImplementation(projects.demeterProfilerBase) // benchmarkable
    androidTestImplementation(projects.demeterTracerProfilerPlugin) // benchmarkable
    androidTestImplementation(projects.demeterInjectProfilerPlugin) // benchmarkable
}
