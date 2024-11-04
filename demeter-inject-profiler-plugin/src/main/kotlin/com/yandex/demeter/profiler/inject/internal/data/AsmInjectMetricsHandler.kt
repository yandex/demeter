package com.yandex.demeter.profiler.inject.internal.data

import com.yandex.demeter.profiler.inject.internal.asm.InjectAsm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map

/**
 * Class responsible for handling raw metrics in background
 */
internal object AsmInjectMetricsHandler {
    private var handleJob: Job? = null

    fun init(consumerScope: CoroutineScope) {
        handleJob?.cancel()
        InjectMetricsRepository.clear()
        InjectAsm.metricsQueue
            .map(InjectMetricsRepository::putMetric)
            .launchIn(consumerScope)
    }
}
