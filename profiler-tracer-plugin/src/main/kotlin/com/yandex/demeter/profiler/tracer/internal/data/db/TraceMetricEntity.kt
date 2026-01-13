package com.yandex.demeter.profiler.tracer.internal.data.db

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
data class TraceMetricEntity(
    val id: String,
    val className: String,
    val methodName: String,
    val count: Int,
    val maxDurationMs: Long,
    val lastDurationMs: Long,
    val lastStartTimeMs: Long,
    val lastThreadName: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val simpleName: String
        get() = "$className#$methodName"
}
