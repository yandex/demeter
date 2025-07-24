package com.yandex.demeter.plugin

import com.yandex.demeter.compose.plugin.DemeterComposeExtension
import com.yandex.demeter.inject.plugin.DemeterInjectExtension
import com.yandex.demeter.tracer.plugin.DemeterTracerExtension

open class DemeterExtension {

    internal var tracerConfig: DemeterTracerExtension? = null
    internal var injectConfig: DemeterInjectExtension? = null
    internal var composeConfig: DemeterComposeExtension? = null

    fun tracer(configure: DemeterTracerExtension.() -> Unit = {}) {
        tracerConfig = DemeterTracerExtension().apply(configure)
    }

    fun inject(configure: DemeterInjectExtension.() -> Unit = {}) {
        injectConfig = DemeterInjectExtension().apply(configure)
    }

    fun compose(configure: DemeterComposeExtension.() -> Unit = {}) {
        composeConfig = DemeterComposeExtension().apply(configure)
    }

    companion object {
        val name = "demeter"
    }
}
