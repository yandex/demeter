package plugins.module.android

import BuildConfig
import extensions.androidApp
import extensions.kotlin
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
}

group = BuildConfig.demeterGroup
version = BuildConfig.demeterVersion

androidApp {
    compileSdk {
        version = release(BuildConfig.compileSdkMajor) {
            minorApiLevel = BuildConfig.compileSdkMinor
        }
    }

    defaultConfig {
        minSdk = BuildConfig.minSdk
    }

    kotlin {
        jvmToolchain(BuildConfig.javaVersion)
    }
}
