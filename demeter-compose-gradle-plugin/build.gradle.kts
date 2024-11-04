import extensions.plugin

plugins {
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
}

plugin(
    name = "demeter-compose",
    pluginId = "com.yandex.demeter.compose",
    implementation = "com.yandex.demeter.compose.plugin.DemeterComposePlugin",
)

dependencies {
    api(projects.gradlePluginUtils)

    api(libs.kotlin.gradlePluginApi)
}
