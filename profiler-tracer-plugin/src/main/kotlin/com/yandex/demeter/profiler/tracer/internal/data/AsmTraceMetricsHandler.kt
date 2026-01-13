package com.yandex.demeter.profiler.tracer.internal.data

import android.content.Context
import com.yandex.demeter.profiler.tracer.internal.asm.TracerAsm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Class responsible for collecting raw ASM metrics and persisting them to database.
 */
internal object AsmTraceMetricsHandler {
    private var collectJob: Job? = null
    private lateinit var repository: TraceMetricsRepository

    fun init(context: Context, consumerScope: CoroutineScope) {
        repository = TraceMetricsRepositoryImpl.getInstance(context)
        startCollecting(consumerScope)
    }

    private fun startCollecting(scope: CoroutineScope) {
        collectJob?.cancel()
        collectJob = scope.launch {
            repository.clear()
            TracerAsm.metricsQueue.collect { metric ->
                repository.upsertMetric(metric)
                TraceMetricsReportersNotifier.report(metric)
            }
        }
    }
}
