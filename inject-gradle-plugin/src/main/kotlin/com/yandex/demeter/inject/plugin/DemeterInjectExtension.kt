package com.yandex.demeter.inject.plugin

import com.android.build.api.variant.VariantExtensionConfig
import com.yandex.demeter.plugin.FeatureExtension
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

abstract class DemeterInjectExtension @Inject constructor(
    extensionConfig: VariantExtensionConfig<*>,
    project: Project
) : FeatureExtension() {
    abstract val includedClasses: ListProperty<String>
    abstract val excludedClasses: ListProperty<String>

    init {
        val projectExtension = project.extensions
            .findByType(DemeterInjectProjectDslExtension::class.java)
        var buildExtension = extensionConfig
            .buildTypeExtension(DemeterInjectBuildTypeDslExtension::class.java)

        enabled.set(
            buildExtension.enabled ?: projectExtension?.enabled ?: false
        )
        debug.set(
            buildExtension.debug ?: projectExtension?.debug ?: false
        )
        excludedClasses.set(
            buildExtension.excludedClasses ?: projectExtension?.excludedClasses ?: emptyList()
        )
        includedClasses.set(
            buildExtension.includedClasses ?: projectExtension?.includedClasses ?: emptyList()
        )
    }

    override val extensionName
        get() = NAME

    companion object {
        const val NAME = "demeterInject"
    }
}

interface DemeterInjectProjectDslExtension : DemeterInjectDslExtension

interface DemeterInjectBuildTypeDslExtension : DemeterInjectDslExtension

interface DemeterInjectDslExtension {
    var enabled: Boolean?
    var debug: Boolean?

    var includedClasses: List<String>?
    var excludedClasses: List<String>?
}
