package extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

fun Project.plugin(
    pluginId: String,
    name: String = pluginId,
    implementation: String,
) {
    gradlePlugin {
        plugins {
            create(name) {
                id = pluginId
                implementationClass = implementation
            }
        }
    }
    publishPlugin()
}

private fun Project.gradlePlugin(configure: Action<GradlePluginDevelopmentExtension>): Unit =
    extensions.configure("gradlePlugin", configure)
