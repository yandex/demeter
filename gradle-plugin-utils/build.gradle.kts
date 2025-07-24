import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.jvm.base)
    `kotlin-dsl`
    alias(libs.plugins.vanniktech.maven.publish)
}

dependencies {
    implementation(libs.tools.gradleApi)
    implementation(libs.asm.commons)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Plugin Utils") }
}
