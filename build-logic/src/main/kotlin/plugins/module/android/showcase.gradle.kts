package plugins.module.android

import BuildConfig
import extensions.androidApp
import extensions.kotlin
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    kotlin("kapt")
}

group = BuildConfig.demeterGroup
version = BuildConfig.demeterVersion

androidApp {
    compileSdk = BuildConfig.compileSdk

    defaultConfig {
        minSdk = BuildConfig.minSdk
    }

    kotlin {
        jvmToolchain(BuildConfig.javaVersion)
    }
}
