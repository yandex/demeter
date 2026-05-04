import extensions.configurePom
import extensions.configureRepository
import extensions.includeTests

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yandex.demeter.profiler.coroutine.tracer"

    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testOptions.unitTests.isReturnDefaultValues = true
}

includeTests()

dependencies {
    api(projects.profilerBase)

    implementation(libs.coroutines)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(projects.profilerUi)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Coroutine Tracer Plugin") }
}
