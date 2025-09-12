package com.yandex.demeter.plugin

import com.android.build.api.variant.VariantExtension
import org.gradle.api.provider.Property

abstract class FeatureExtension : VariantExtension, java.io.Serializable {
    abstract val extensionName: String

    abstract val enabled: Property<Boolean>
    abstract val debug: Property<Boolean>
}
