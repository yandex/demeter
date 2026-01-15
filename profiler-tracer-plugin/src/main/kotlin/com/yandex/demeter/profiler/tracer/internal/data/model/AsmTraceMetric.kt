package com.yandex.demeter.profiler.tracer.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi

private const val NANOSECONDS_IN_MILLISECOND = 1_000_000L

@InternalDemeterApi
class AsmTraceMetric(
    val id: String,
    val className: String,
    val methodName: String,
    val startTimeNs: Long,
    val finishTimeNs: Long,
    val threadName: String,
    // Unique ID for this specific method invocation
    val executionId: Long,
    // Links to parent invocation in call chain (null for root calls)
    val parentExecutionId: Long?,
    // Parent method identifier (null for root calls)
    val parentMethodId: String?,
    // Nesting level in call stack (0 = root)
    val depth: Int,
) {

    private inline val durationNs: Long get() = finishTimeNs - startTimeNs

    val startTimeMs: Long get() = startTimeNs / NANOSECONDS_IN_MILLISECOND
    val durationMs: Long get() = durationNs / NANOSECONDS_IN_MILLISECOND
}
