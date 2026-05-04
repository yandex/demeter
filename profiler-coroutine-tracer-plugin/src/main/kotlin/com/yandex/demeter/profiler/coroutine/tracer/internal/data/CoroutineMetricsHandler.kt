package com.yandex.demeter.profiler.coroutine.tracer.internal.data

import android.content.Context
import com.yandex.demeter.profiler.coroutine.tracer.internal.asm.CoroutineTracerAsm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Class responsible for collecting raw coroutine ASM metrics and persisting them to database.
 */
internal object CoroutineMetricsHandler {
    private var collectJob: Job? = null
    private lateinit var repository: CoroutineMetricsRepository

    fun init(context: Context, consumerScope: CoroutineScope) {
        repository = CoroutineMetricsRepositoryImpl.getInstance(context)
        startCollecting(consumerScope)
    }

    private fun startCollecting(scope: CoroutineScope) {
        collectJob?.cancel()
        collectJob = scope.launch {
            repository.clear()
            CoroutineTracerAsm.metricsQueue.collect { metric ->
                repository.upsertMetric(metric)
                CoroutineMetricsReportersNotifier.report(metric)
            }
        }
    }
}
