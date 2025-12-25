import extensions.configurePom
import extensions.configureRepository
import extensions.includeTests

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
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
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Tracer Profiler Plugin") }
}
