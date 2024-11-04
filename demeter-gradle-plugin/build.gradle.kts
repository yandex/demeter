import extensions.plugin

plugins {
    alias(libs.plugins.module.jvm.base).apply(false)
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
}

plugin(
    name = "demeter",
    pluginId = "com.yandex.demeter",
    implementation = "com.yandex.demeter.plugin.DemeterPlugin",
)

dependencies {
    api(projects.tracerGradlePlugin)
    api(projects.injectGradlePlugin)
    api(projects.composeGradlePlugin)

    implementation(libs.tools.gradle)
}

val rootPublishTask = tasks.getByName("publish")
subprojects.forEach { subproject ->
    subproject.afterEvaluate {
        subproject.tasks.findByName("publish")?.let { rootPublishTask.dependsOn(it) }
    }
}
