import com.yandex.demeter.compose.plugin.DemeterComposeBuildTypeDslExtension

plugins {
    alias(libs.plugins.module.android.compose)
    id("com.yandex.demeter.compose")
}

android {
    namespace = "com.yandex.demeter.showcase.feature"

    buildTypes {
        getByName("debug") {
            extensions.configure<DemeterComposeBuildTypeDslExtension>("demeterCompose") {
                enabled = true
            }
        }
    }
}

dependencies {
    implementation(libs.bundles.compose)
    debugCompileOnly(projects.profilerComposePlugin)
}
