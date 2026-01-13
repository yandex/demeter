package com.yandex.demeter.profiler.tracer.internal.data.db

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric

@InternalDemeterApi
class TraceMetricWrapper(
    private val entity: TraceMetricEntity
) : TimeMetric {
    override val simpleName: String
        get() = entity.simpleName

    override val totalInitTime: Long
        get() = entity.maxDurationMs

    override val threadName: String
        get() = entity.lastThreadName

    override val args: List<TimeMetric>
        get() = emptyList()

    val wrapped: TraceMetricEntity
        get() = entity
}

@InternalDemeterApi
fun TraceMetricEntity.asTimeMetric(): TraceMetricWrapper = TraceMetricWrapper(this)

@InternalDemeterApi
fun List<TraceMetricEntity>.asTimeMetrics(): List<TraceMetricWrapper> = map { it.asTimeMetric() }
