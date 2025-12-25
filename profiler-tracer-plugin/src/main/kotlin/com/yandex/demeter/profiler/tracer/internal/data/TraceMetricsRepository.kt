package com.yandex.demeter.profiler.tracer.internal.data

import androidx.collection.MutableScatterMap
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.tracer.internal.data.model.TraceMetric

/**
 * Contains trace metrics.
 */
@InternalDemeterApi
object TraceMetricsRepository {
    private const val INITIAL_CAPACITY = 1 shl 14

    private val _metrics = MutableScatterMap<String, TraceMetric>(INITIAL_CAPACITY)
    val metrics: Map<String, TraceMetric>
        get() = _metrics.asMap()

    fun getOrPutMetric(id: String, defaultValue: () -> TraceMetric): TraceMetric {
        return _metrics.getOrPut(id, defaultValue)
    }

    fun clear() {
        _metrics.clear()
    }
}
