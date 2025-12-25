package com.yandex.demeter.profiler.compose

import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.profiler.compose.internal.data.ComposeMetricHolder
import com.yandex.demeter.profiler.compose.internal.ir.tracer.ComposeTracerMetricHolder
import kotlinx.coroutines.CoroutineScope

class ComposeDemeterPlugin : DemeterPlugin {
    override val id: String = PLUGIN_NAME

    override fun init(consumerScope: CoroutineScope) {
        ComposeMetricHolder.init(consumerScope)
        ComposeTracerMetricHolder.init(consumerScope)
    }

    companion object {
        const val PLUGIN_NAME = "com.yandex.demeter.profiler.compose"
    }
}
