import extensions.plugin

plugins {
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
}

plugin(
    name = "demeter-inject",
    pluginId = "com.yandex.demeter.inject",
    implementation = "com.yandex.demeter.inject.plugin.DemeterInjectPlugin",
)

dependencies {
    api(projects.gradlePluginUtils)

    implementation(libs.tools.gradleApi)
    implementation(libs.asm.commons)
    implementation(libs.asm.utils)
}
