import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

android.namespace = "com.yandex.demeter.profiler.base"

dependencies {
    api(projects.core)

    implementation(libs.kotlin.reflect)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Profiler Base") }
}
