import extensions.configurePom
import extensions.configureRepository
import extensions.includeTests

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.tracer"

    viewBinding.enable = true

    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testOptions.unitTests.isReturnDefaultValues = true
}

includeTests()

dependencies {
    implementation(projects.profilerBase)
    implementation(projects.profilerUi)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)
    implementation(libs.androidx.collection)

    implementation(libs.androidx.constraintLayout)
    implementation(libs.fastadapter.core)
    implementation(libs.fastadapter.extensionExpandable)
    implementation(libs.fastScroll)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Tracer Profiler Plugin") }
}
