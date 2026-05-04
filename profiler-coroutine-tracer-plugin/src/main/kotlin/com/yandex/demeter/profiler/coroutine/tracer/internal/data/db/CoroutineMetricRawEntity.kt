package com.yandex.demeter.profiler.coroutine.tracer.internal.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.RawTraceMetric

@InternalDemeterApi
@Entity(
    tableName = "coroutine_metrics_raw",
    indices = [
        Index(value = ["parentTraceId"]),
        Index(value = ["startTimeMs"]),
    ]
)
data class CoroutineMetricRawEntity(
    @PrimaryKey val traceId: Long,
    val parentTraceId: Long?,
    val launchSite: String,
    val durationMs: Long,
    val startTimeMs: Long,
    val launchThreadName: String,
    val completionThreadName: String,
    val isCancelled: Boolean,
    val exception: String?,
    val depth: Int,
    val dispatcherName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@InternalDemeterApi
fun CoroutineMetricRawEntity.asRawTraceMetric() = RawTraceMetric(
    methodId = launchSite,
    className = launchSite.substringBeforeLast('#', launchSite),
    methodName = launchSite.substringAfterLast('#', ""),
    durationMs = durationMs,
    startTimeMs = startTimeMs,
    threadName = launchThreadName,
    executionId = traceId,
    parentExecutionId = parentTraceId,
    parentMethodId = null,
    depth = depth,
)

@InternalDemeterApi
fun List<CoroutineMetricRawEntity>.asRawTraceMetrics(): List<RawTraceMetric> = map { it.asRawTraceMetric() }
