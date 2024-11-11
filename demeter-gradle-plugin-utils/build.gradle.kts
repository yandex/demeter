import extensions.publishLib

plugins {
    alias(libs.plugins.module.jvm.base)
    `kotlin-dsl`
}

publishLib("gradle-plugin-utils")

dependencies {
    implementation(libs.tools.gradleApi)
    implementation(libs.asm.commons)
}
