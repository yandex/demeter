import extensions.publishLib

plugins {
    alias(libs.plugins.module.android.base)
}

android.namespace = "com.yandex.demeter.flipper"

publishLib("flipper")

dependencies {
    api(projects.demeterCore)

    implementation(libs.flipper.core)
}
