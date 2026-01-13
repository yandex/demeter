import extensions.configurePom
import extensions.configureRepository
import extensions.includeTests

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yandex.demeter.profiler.tracer"

    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testOptions.unitTests.isReturnDefaultValues = true
}

includeTests()

dependencies {
    api(projects.profilerBase)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)
    implementation(libs.androidx.collection)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    api(libs.paging.common)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Tracer Profiler Plugin") }
}
