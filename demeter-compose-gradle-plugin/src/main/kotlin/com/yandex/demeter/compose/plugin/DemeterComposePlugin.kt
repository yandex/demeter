package com.yandex.demeter.compose.plugin

import com.yandex.demeter.plugin.requireAndroidApp
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class DemeterComposePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.requireAndroidApp()

        target.extensions.create(DemeterComposeExtension.NAME, DemeterComposeExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String =
        "com.yandex.demeter.compose-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.yandex.demeter",
        artifactId = "compose-compiler-plugin",
        version = "0.38.0",
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider { emptyList() }
    }
}
