package com.yandex.demeter.profiler.tracer.internal.data

import com.yandex.demeter.Reporter
import com.yandex.demeter.profiler.tracer.internal.data.model.TraceMetric
import java.time.Instant
import java.time.ZoneId

/**
 * Sends metrics to configured reporters (like Flipper app).
 */
internal object TraceMetricsReportersNotifier {
    private const val PAYLOAD_PARAMETERS_COUNT = 7

    private var reporters: List<Reporter> = emptyList()

    fun init(reporters: List<Reporter>) {
        this.reporters = reporters
    }

    internal fun report(traceMetric: TraceMetric) {
        if (reporters.isNotEmpty()) {
            val payload = HashMap<String, Any>(PAYLOAD_PARAMETERS_COUNT).apply {
                put("id", traceMetric.id)
                put(
                    "timestamp",
                    Instant.ofEpochMilli(traceMetric.startTimes.last())
                        .atZone(ZoneId.systemDefault()).toLocalTime()
                )
                put("className", traceMetric.className)
                put("methodName", traceMetric.methodName)
                put("ms", traceMetric.durations.last()) // key used in demeter-flipper
                put("count", traceMetric.count)
                put("thread", traceMetric.threadName)
            }
            reporters.forEach { it.report(payload) }
        }
    }
}
