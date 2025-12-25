package com.yandex.demeter.profiler.inject.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric

private const val NANOSECONDS_IN_MILLISECOND = 1_000_000L

@InternalDemeterApi
class AsmInjectMetric(
    val initializedClass: Class<*>,
    val className: String,
    val startTimeNs: Long,
    val finishTimeNs: Long,
    override val threadName: String,
) : TimeMetric {

    private inline val durationNs: Long get() = finishTimeNs - startTimeNs

    val durationMs: Long get() = durationNs / NANOSECONDS_IN_MILLISECOND

    override val totalInitTime: Long
        get() = durationMs
    override val simpleName: String
        get() = className
    override val args: List<TimeMetric> = emptyList()
}
