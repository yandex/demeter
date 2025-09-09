package com.yandex.demeter.profiler.tracer

import android.content.Context
import android.view.View
import com.yandex.demeter.Reporter
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.tracer.internal.data.AsmTraceMetricsHandler
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsReportersNotifier
import com.yandex.demeter.profiler.tracer.internal.ui.TracerPluginView
import kotlinx.coroutines.CoroutineScope

class TracerDemeterPlugin(
    private val reporters: List<Reporter> = emptyList(),
    private val isEnabledOnStart: Boolean = true
) : UiDemeterPlugin {
    override val id: String get() = "com.yandex.demeter.profiler.tracer"
    override val name: String get() = "Tracer"

    override fun init(consumerScope: CoroutineScope) {
        if (isEnabledOnStart) {
            com.yandex.demeter.profiler.tracer.internal.asm.EnabledValueHolder.isEnabled = true
        }
        TraceMetricsReportersNotifier.init(reporters)
        AsmTraceMetricsHandler.init(consumerScope)
    }

    override fun ui(context: Context): View = TracerPluginView(context)
}
