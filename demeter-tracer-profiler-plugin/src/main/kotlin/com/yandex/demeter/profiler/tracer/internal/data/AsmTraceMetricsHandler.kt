package com.yandex.demeter.profiler.tracer.internal.data

import com.yandex.demeter.profiler.tracer.internal.asm.TracerAsm
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import com.yandex.demeter.profiler.tracer.internal.data.model.TraceMetric
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Class responsible for mapping raw metrics in background
 */
internal object AsmTraceMetricsHandler {
    private var handleJob: Job? = null

    fun init(consumerScope: CoroutineScope) {
        collectMetrics(consumerScope)
    }

    private fun collectMetrics(consumerScope: CoroutineScope) {
        handleJob?.cancel()
        TraceMetricsRepository.clear()
        handleJob = TracerAsm.metricsQueue
            .onEach(this::handleMetric)
            .launchIn(consumerScope)
    }

    private fun handleMetric(asmMetric: AsmTraceMetric) {
        val traceMetric = TraceMetricsRepository.getOrPutMetric(asmMetric.id) {
            TraceMetric(
                asmMetric.id,
                asmMetric.className,
                asmMetric.methodName
            )
        }

        traceMetric.startTimes.add(asmMetric.startTimeMs)
        traceMetric.durations.add(asmMetric.durationMs)
        traceMetric.threadNames.add(asmMetric.threadName)

        TraceMetricsReportersNotifier.report(traceMetric)
    }
}
