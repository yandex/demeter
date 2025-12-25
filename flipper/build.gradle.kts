import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android.namespace = "com.yandex.demeter.flipper"

dependencies {
    api(projects.core)
    implementation(projects.profilerBase)

    implementation(libs.flipper.core)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Flipper") }
}
