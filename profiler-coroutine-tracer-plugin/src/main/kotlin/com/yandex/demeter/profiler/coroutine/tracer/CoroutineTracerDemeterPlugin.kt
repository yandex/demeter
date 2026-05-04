package com.yandex.demeter.profiler.coroutine.tracer

import android.content.Context
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.CoroutineMetricsHandler
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.CoroutineMetricsReportersNotifier
import kotlinx.coroutines.CoroutineScope

class CoroutineTracerDemeterPlugin(
    private val context: Context,
    private val reporter: DemeterReporter? = null,
) : DemeterPlugin {
    override val id: String = PLUGIN_NAME

    override fun init(consumerScope: CoroutineScope) {
        reporter?.let(CoroutineMetricsReportersNotifier::init)
        CoroutineMetricsHandler.init(context, consumerScope)
    }

    companion object {
        const val PLUGIN_NAME = "com.yandex.demeter.profiler.coroutine.tracer"
    }
}
