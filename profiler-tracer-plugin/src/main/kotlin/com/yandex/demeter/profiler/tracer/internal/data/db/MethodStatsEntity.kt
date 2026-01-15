package com.yandex.demeter.profiler.tracer.internal.data.db

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
data class MethodStatsEntity(
    val methodId: String,
    val className: String,
    val methodName: String,
    val callCount: Int,
    val avgDurationMs: Double,
    val maxDurationMs: Long,
    val minDurationMs: Long,
    val avgDepth: Double,
    val maxDepth: Int,
) {
    val simpleName: String get() = "$className#$methodName"
}
