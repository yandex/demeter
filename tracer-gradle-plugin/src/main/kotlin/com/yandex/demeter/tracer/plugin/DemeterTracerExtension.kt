package com.yandex.demeter.tracer.plugin

import com.android.build.api.variant.VariantExtensionConfig
import com.yandex.demeter.plugin.FeatureExtension
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

abstract class DemeterTracerExtension @Inject constructor(
    extensionConfig: VariantExtensionConfig<*>,
    project: Project
) : FeatureExtension() {
    abstract val includedClasses: ListProperty<String>
    abstract val excludedClasses: ListProperty<String>

    init {
        val projectExtension = project.extensions
            .findByType(DemeterTracerProjectDslExtension::class.java)
        var buildExtension = extensionConfig
            .buildTypeExtension(DemeterTracerBuildTypeDslExtension::class.java)

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
        const val NAME = "demeterTracer"
    }
}

interface DemeterTracerProjectDslExtension : DemeterTracerDslExtension

interface DemeterTracerBuildTypeDslExtension : DemeterTracerDslExtension

interface DemeterTracerDslExtension {
    var enabled: Boolean?
    var debug: Boolean?

    var includedClasses: List<String>?
    var excludedClasses: List<String>?
}
