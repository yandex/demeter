package com.yandex.demeter.profiler.tracer.internal.data

import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object TraceMetricsReportersNotifier {
    private const val PAYLOAD_PARAMETERS_COUNT = 6

    private var reporter: DemeterReporter? = null

    fun init(reporter: DemeterReporter) {
        this.reporter = reporter
    }

    fun report(metric: AsmTraceMetric) {
        reporter?.let {
            val payload = HashMap<String, Any>(PAYLOAD_PARAMETERS_COUNT).apply {
                put("id", metric.id)
                put(
                    "timestamp",
                    SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
                        .format(Date(metric.startTimeMs))
                )
                put("className", metric.className)
                put("methodName", metric.methodName)
                put("ms", metric.durationMs)
                put("thread", metric.threadName)
            }
            it.report(payload)
        }
    }
}
