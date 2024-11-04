plugins {
    alias(libs.plugins.module.android.showcase)

    id("com.yandex.demeter")
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.yandex.demeter:compose-compiler-plugin"))
            .using(project(":demeter-compose-compiler-plugin"))
    }
}

demeter {
    tracer {
        includedClasses = listOf("com.yandex.demeter.showcase")
    }
    inject {
        includedClasses = listOf("com.yandex.demeter.showcase")
    }
    compose()
}

android {
    namespace = "com.yandex.demeter"

    defaultConfig {
        applicationId = "com.yandex.demeter"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.demeterProfiler)
    implementation(projects.demeterCore)

    implementation(projects.demeterTracerProfilerPlugin)
    implementation(projects.demeterInjectProfilerPlugin)
    implementation(projects.demeterComposeProfilerPlugin)

    kapt(libs.daggerCompiler)
    implementation(libs.dagger)
    implementation(libs.javaXInject)

    implementation(libs.androidx.core)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.viewPager2)
    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.material)

    implementation(libs.coroutines)

    implementation(libs.bundles.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    testImplementation(libs.junit.core)
}

composeCompiler {
    enableStrongSkippingMode = true

    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}
