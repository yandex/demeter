import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.inject"

    viewBinding.enable = true
}

dependencies {
    api(projects.profilerBase)
    api(projects.profilerUi)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)

    implementation(libs.androidx.core)
    implementation(libs.androidx.constraintLayout)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Inject Profiler Plugin") }
}
