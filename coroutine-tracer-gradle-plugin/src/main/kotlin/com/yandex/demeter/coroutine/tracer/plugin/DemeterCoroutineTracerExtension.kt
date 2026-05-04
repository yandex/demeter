package com.yandex.demeter.coroutine.tracer.plugin

import com.android.build.api.variant.VariantExtensionConfig
import com.yandex.demeter.plugin.FeatureExtension
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

abstract class DemeterCoroutineTracerExtension @Inject constructor(
    extensionConfig: VariantExtensionConfig<*>,
    project: Project
) : FeatureExtension() {
    abstract val includedClasses: ListProperty<String>
    abstract val excludedClasses: ListProperty<String>

    init {
        val projectExtension = project.extensions
            .findByType(DemeterCoroutineTracerProjectDslExtension::class.java)
        var buildExtension = extensionConfig
            .buildTypeExtension(DemeterCoroutineTracerBuildTypeDslExtension::class.java)

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
        const val NAME = "demeterCoroutineTracer"
    }
}

interface DemeterCoroutineTracerProjectDslExtension : DemeterCoroutineTracerDslExtension

interface DemeterCoroutineTracerBuildTypeDslExtension : DemeterCoroutineTracerDslExtension

interface DemeterCoroutineTracerDslExtension {
    var enabled: Boolean?
    var debug: Boolean?

    var includedClasses: List<String>?
    var excludedClasses: List<String>?
}
