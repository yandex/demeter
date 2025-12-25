package com.yandex.demeter.profiler.tracer.internal.data

import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.internal.reporter.MetricsReportersNotifier
import com.yandex.demeter.profiler.tracer.internal.data.model.TraceMetric
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object TraceMetricsReportersNotifier : MetricsReportersNotifier<TraceMetric> {
    private const val PAYLOAD_PARAMETERS_COUNT = 7

    private var reporter: DemeterReporter? = null

    fun init(reporter: DemeterReporter) {
        this.reporter = reporter
    }

    override fun report(metric: TraceMetric) {
        reporter?.let {
            val payload = HashMap<String, Any>(PAYLOAD_PARAMETERS_COUNT).apply {
                put("id", metric.id)
                put(
                    "timestamp",
                    SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
                        .format(Date(metric.startTimes.last()))
                )
                put("className", metric.className)
                put("methodName", metric.methodName)
                put("ms", metric.durations.last()) // key used in demeter-flipper
                put("count", metric.count)
                put("thread", metric.threadName)
            }
            it.report(payload)
        }
    }
}
