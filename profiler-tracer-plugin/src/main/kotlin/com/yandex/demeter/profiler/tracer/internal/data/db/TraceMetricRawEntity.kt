package com.yandex.demeter.profiler.tracer.internal.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
@Entity(
    tableName = "trace_metrics_raw",
    indices = [Index(value = ["methodId"])]
)
data class TraceMetricRawEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val methodId: String,
    val className: String,
    val methodName: String,
    val durationMs: Long,
    val startTimeMs: Long,
    val threadName: String,
    val createdAt: Long = System.currentTimeMillis()
)
