import extensions.publishLib

plugins {
    alias(libs.plugins.module.jvm.base)
}

publishLib("compose-compiler-plugin")

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.compiler.testing)
    testImplementation(libs.compose.runtime)
}
