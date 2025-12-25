plugins {
    alias(libs.plugins.module.android.showcase)
    alias(libs.plugins.ksp)

    id("com.yandex.demeter")
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.yandex.demeter:compose-compiler-plugin"))
            .using(project(":compose-compiler-plugin"))
    }
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
        getByName("debug") {
            demeter {
                tracer {
                    includedClasses = listOf("com.yandex.demeter.showcase.ui")
                }
                inject {
                    includedClasses = listOf("com.yandex.demeter.showcase")
                }

                compose()
            }
        }

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
    implementation(projects.profiler)
    implementation(projects.core)

    implementation(projects.profilerTracerUiPlugin)
    implementation(projects.profilerInjectUiPlugin)
    implementation(projects.profilerComposeUiPlugin)

    ksp(libs.daggerCompiler)
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
