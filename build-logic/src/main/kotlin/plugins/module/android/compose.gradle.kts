package plugins.module.android

import extensions.androidLib
import extensions.composeCompiler
import extensions.implementation
import extensions.libs

plugins {
    id("plugins.module.android.base")
    kotlin("plugin.compose")
}

androidLib {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.compose.runtime)
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
