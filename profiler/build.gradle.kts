import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base).apply(true)
    alias(libs.plugins.module.android.compose).apply(false)
    alias(libs.plugins.vanniktech.maven.publish)
}

android.namespace = "com.yandex.demeter.profiler"

dependencies {
    api(projects.profilerBase)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Profiler") }
}
