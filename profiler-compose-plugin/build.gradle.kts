import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.compose"
}

dependencies {
    api(projects.profilerBase)

    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.collection)

    implementation(libs.compose.animation)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Compose Profiler Plugin") }
}
