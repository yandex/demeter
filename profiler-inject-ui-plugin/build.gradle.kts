import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.inject.ui"

    viewBinding.enable = true
}

dependencies {
    api(projects.profilerInjectPlugin)
    api(projects.profilerUi)

    implementation(libs.coroutines)
    implementation(libs.androidx.core)
    implementation(libs.androidx.constraintLayout)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Inject Profiler UI Plugin") }
}
