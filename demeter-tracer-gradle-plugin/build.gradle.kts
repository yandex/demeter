import extensions.includeTests
import extensions.plugin

plugins {
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
}

plugin(
    name = "demeter-tracer",
    pluginId = "com.yandex.demeter.tracer",
    implementation = "com.yandex.demeter.tracer.plugin.DemeterTracerPlugin",
)

includeTests()

dependencies {
    api(projects.gradlePluginUtils)

    implementation(libs.tools.gradle)
    implementation(libs.tools.gradleApi)
    implementation(libs.asm.commons)
    implementation(libs.asm.utils)

    testImplementation(gradleTestKit())
}
