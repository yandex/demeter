import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.tracer.ui"

    viewBinding.enable = true
}

dependencies {
    api(projects.profilerTracerPlugin)
    api(projects.profilerUi)

    implementation(libs.coroutines)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.fastadapter.core)
    implementation(libs.fastadapter.extensionExpandable)
    implementation(libs.fastScroll)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Tracer Profiler UI Plugin") }
}
