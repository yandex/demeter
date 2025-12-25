package com.yandex.demeter.profiler.tracer

import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.profiler.tracer.internal.data.AsmTraceMetricsHandler
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsReportersNotifier
import kotlinx.coroutines.CoroutineScope

class TracerDemeterPlugin(
    private val reporter: DemeterReporter? = null,
) : DemeterPlugin {
    override val id: String = PLUGIN_NAME

    override fun init(consumerScope: CoroutineScope) {
        reporter?.let(TraceMetricsReportersNotifier::init)
        AsmTraceMetricsHandler.init(consumerScope)
    }

    companion object {
        const val PLUGIN_NAME = "com.yandex.demeter.profiler.tracer"
    }
}
