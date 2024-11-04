package com.yandex.demeter.profiler.inject.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi

private const val NANOSECONDS_IN_MILLISECOND = 1_000_000L

@InternalDemeterApi
class AsmInjectMetric(
    val initializedClass: Class<*>,
    val className: String,
    val startTimeNs: Long,
    val finishTimeNs: Long,
    val threadName: String,
) {

    private inline val durationNs: Long get() = finishTimeNs - startTimeNs

    val durationMs: Long get() = durationNs / NANOSECONDS_IN_MILLISECOND
}
