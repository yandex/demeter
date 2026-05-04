import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.coroutine.tracer.ui"
}

dependencies {
    api(projects.profilerCoroutineTracerPlugin)
    api(projects.profilerUi)

    implementation(projects.profilerBase)

    implementation(libs.bundles.compose)

    implementation(libs.coroutines)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = false,
        )
    )

    publishing { configureRepository() }
    pom { configurePom("Demeter Coroutine Tracer UI Plugin") }
}
