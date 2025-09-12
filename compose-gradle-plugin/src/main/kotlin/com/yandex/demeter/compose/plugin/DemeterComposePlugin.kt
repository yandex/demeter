package com.yandex.demeter.compose.plugin

import com.android.build.api.variant.DslExtension
import com.yandex.demeter.compose.plugin.DemeterComposeExtension.Companion.NAME
import com.yandex.demeter.plugin.android
import com.yandex.demeter.plugin.androidComponents
import com.yandex.demeter.plugin.requireAndroidApp
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class DemeterComposePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.requireAndroidApp()
        target.extensions.create(NAME, DemeterComposeProjectDslExtension::class.java)

        target.androidComponents {
            registerExtension(
                DslExtension.Builder(NAME)
                    .extendBuildTypeWith(DemeterComposeBuildTypeDslExtension::class.java)
                    .build()
            ) { config ->
                target.objects.newInstance(
                    DemeterComposeExtension::class.java,
                    config
                )
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.platformType in setOf(
            KotlinPlatformType.jvm,
            KotlinPlatformType.androidJvm
        )

    override fun getCompilerPluginId(): String =
        "demeter-compose-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.yandex.demeter",
        artifactId = "compose-compiler-plugin",
        version = "1.0.0",
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            val extension =
                kotlinCompilation.project.android.buildTypes.findByName(kotlinCompilation.compilationName)
                    ?.extensions?.findByType(DemeterComposeBuildTypeDslExtension::class.java)
                    ?: kotlinCompilation.project.extensions.findByType(
                        DemeterComposeProjectDslExtension::class.java
                    )

            val enabled = extension?.enabled ?: false

            listOf(
                SubpluginOption(
                    "enabled",
                    enabled.toString()
                )
            )
        }
    }
}
