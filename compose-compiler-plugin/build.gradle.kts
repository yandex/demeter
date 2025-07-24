import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.jvm.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.compiler.testing)
    testImplementation(libs.compose.runtime)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Compose Compiler Plugin") }
}
