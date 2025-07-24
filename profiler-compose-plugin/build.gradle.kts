import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.compose"

    viewBinding.enable = true
}

dependencies {
    api(projects.profilerBase)
    api(projects.profilerUi)

    implementation(libs.kotlin.reflect)

    implementation(libs.compose.animation)

    implementation(libs.fastadapter.core)
    implementation(libs.fastScroll)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Compose Profiler Plugin") }
}
