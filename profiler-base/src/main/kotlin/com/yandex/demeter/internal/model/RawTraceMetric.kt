package com.yandex.demeter.internal.model

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
data class RawTraceMetric(
    val methodId: String,
    val className: String,
    val methodName: String,
    val durationMs: Long,
    val startTimeMs: Long,
    val threadName: String,
    val executionId: Long,
    val parentExecutionId: Long?,
    val parentMethodId: String?,
    val depth: Int,
) {
    val simpleName: String get() = "$className#$methodName"
}
