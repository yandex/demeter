package com.yandex.demeter.compose.plugin

import com.android.build.api.variant.VariantExtensionConfig
import com.yandex.demeter.plugin.FeatureExtension
import org.gradle.api.Project
import javax.inject.Inject

abstract class DemeterComposeExtension @Inject constructor(
    extensionConfig: VariantExtensionConfig<*>,
    project: Project
) : FeatureExtension() {

    init {
        val projectExtension = project.extensions
            .findByType(DemeterComposeProjectDslExtension::class.java)
        var buildExtension = extensionConfig
            .buildTypeExtension(DemeterComposeBuildTypeDslExtension::class.java)

        enabled.set(
            buildExtension.enabled ?: projectExtension?.enabled ?: false
        )
        debug.set(
            buildExtension.debug ?: projectExtension?.debug ?: false
        )
    }

    override val extensionName
        get() = NAME

    companion object {
        const val NAME = "demeterCompose"
    }
}

interface DemeterComposeProjectDslExtension : DemeterComposeDslExtension

interface DemeterComposeBuildTypeDslExtension : DemeterComposeDslExtension

interface DemeterComposeDslExtension {
    var enabled: Boolean?
    var debug: Boolean?
}
