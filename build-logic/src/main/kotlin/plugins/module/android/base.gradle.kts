package plugins.module.android

import BuildConfig
import extensions.androidLib
import extensions.kotlin
import extensions.optInInternalDemeterApi
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.library")
    kotlin("android")
}

group = BuildConfig.demeterGroup
version = BuildConfig.demeterVersion

androidLib {
    compileSdk = BuildConfig.compileSdk

    defaultConfig {
        minSdk = BuildConfig.minSdk
        consumerProguardFiles("consumer-rules.pro")
    }

    kotlin {
        jvmToolchain(BuildConfig.javaVersion)
    }
}

optInInternalDemeterApi()
