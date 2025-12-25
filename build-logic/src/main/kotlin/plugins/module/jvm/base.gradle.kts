package plugins.module.jvm

import BuildConfig
import extensions.java
import extensions.optInInternalDemeterApi
import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
}

group = BuildConfig.demeterGroup
version = BuildConfig.demeterVersion

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(BuildConfig.javaVersion)
}

optInInternalDemeterApi()
