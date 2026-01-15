package com.yandex.demeter.profiler.tracer.internal.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.RawTraceMetric

@InternalDemeterApi
@Entity(
    tableName = "trace_metrics_raw",
    indices = [
        Index(value = ["methodId"]),
        Index(value = ["executionId"]),
        Index(value = ["parentExecutionId"]),
        Index(value = ["threadName", "startTimeMs"]),
        Index(value = ["depth"])
    ]
)
data class TraceMetricRawEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val methodId: String,
    val className: String,
    val methodName: String,
    val durationMs: Long,
    val startTimeMs: Long,
    val threadName: String,
    val executionId: Long,
    // Links to parent invocation in call chain (null for root calls)
    val parentExecutionId: Long?,
    // Parent method identifier (null for root calls)
    val parentMethodId: String?,
    // Nesting level in call stack (0 = root)
    val depth: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@InternalDemeterApi
fun TraceMetricRawEntity.asRawTraceMetric() = RawTraceMetric(
    methodId = methodId,
    className = className,
    methodName = methodName,
    durationMs = durationMs,
    startTimeMs = startTimeMs,
    threadName = threadName,
    executionId = executionId,
    parentExecutionId = parentExecutionId,
    parentMethodId = parentMethodId,
    depth = depth,
)

@InternalDemeterApi
fun List<TraceMetricRawEntity>.asRawTraceMetrics(): List<RawTraceMetric> = map { it.asRawTraceMetric() }
