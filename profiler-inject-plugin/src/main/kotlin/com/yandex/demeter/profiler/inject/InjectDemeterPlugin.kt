package com.yandex.demeter.profiler.inject

import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.inject.internal.data.AsmInjectMetricsHandler
import com.yandex.demeter.profiler.inject.internal.data.InjectMetricsReportersNotifier
import kotlinx.coroutines.CoroutineScope

class InjectDemeterPlugin(
    private val reporter: DemeterReporter? = null,
) : DemeterPlugin {

    @InternalDemeterApi
    override val id: String = PLUGIN_NAME

    @InternalDemeterApi
    override fun init(consumerScope: CoroutineScope) {
        reporter?.let(InjectMetricsReportersNotifier::init)
        AsmInjectMetricsHandler.init(consumerScope)
    }

    companion object {
        const val PLUGIN_NAME = "com.yandex.demeter.profiler.inject"
    }
}
