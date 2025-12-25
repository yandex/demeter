import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.inject"
}

dependencies {
    api(projects.profilerBase)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)

    implementation(libs.androidx.core)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Inject Profiler Plugin") }
}
