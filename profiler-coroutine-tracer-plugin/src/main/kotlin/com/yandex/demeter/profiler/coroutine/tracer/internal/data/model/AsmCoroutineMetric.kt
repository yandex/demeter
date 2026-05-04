package com.yandex.demeter.profiler.coroutine.tracer.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi

private const val NANOSECONDS_IN_MILLISECOND = 1_000_000L

@InternalDemeterApi
class AsmCoroutineMetric(
    val traceId: Long,
    val parentTraceId: Long?,
    val launchSite: String,
    val startTimeNs: Long,
    val endTimeNs: Long,
    val launchThreadName: String,
    val completionThreadName: String,
    val isCancelled: Boolean,
    val exception: String?,
    val depth: Int,
    val dispatcherName: String?,
) {

    val durationMs: Long get() = (endTimeNs - startTimeNs) / NANOSECONDS_IN_MILLISECOND
    val startTimeMs: Long get() = startTimeNs / NANOSECONDS_IN_MILLISECOND
}
