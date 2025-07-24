import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.android.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.yandex.demeter.profiler.ui"

    viewBinding.enable = true
}

dependencies {
    api(projects.core)
    api(projects.profilerBase)

    implementation(libs.fastadapter.core)
    implementation(libs.fastadapter.extensionExpandable)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.material)
    implementation(libs.androidx.viewPager2)
    implementation(libs.androidx.collection)

    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)

    implementation(libs.fastScroll)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Profiler UI") }
}
