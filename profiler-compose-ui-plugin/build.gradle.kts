import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.compose.ui"

    viewBinding.enable = true
}

dependencies {
    api(projects.profilerComposePlugin)
    api(projects.profilerUi)

    implementation(libs.fastadapter.core)
    implementation(libs.fastScroll)
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
    pom { configurePom("Demeter Compose Profiler UI Plugin") }
}
