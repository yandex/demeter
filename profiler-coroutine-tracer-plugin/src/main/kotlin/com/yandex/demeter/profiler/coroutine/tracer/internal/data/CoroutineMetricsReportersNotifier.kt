package com.yandex.demeter.profiler.coroutine.tracer.internal.data

import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.AsmCoroutineMetric
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object CoroutineMetricsReportersNotifier {
    private const val PAYLOAD_PARAMETERS_COUNT = 9

    private var reporter: DemeterReporter? = null

    private val timestampFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun init(reporter: DemeterReporter) {
        this.reporter = reporter
    }

    fun report(metric: AsmCoroutineMetric) {
        reporter?.let {
            val payload = HashMap<String, Any>(PAYLOAD_PARAMETERS_COUNT).apply {
                put("traceId", metric.traceId)
                put("timestamp", timestampFormat.format(Date(metric.startTimeMs)))
                put("launchSite", metric.launchSite)
                put("ms", metric.durationMs)
                put("launchThread", metric.launchThreadName)
                put("completionThread", metric.completionThreadName)
                put("isCancelled", metric.isCancelled)
                metric.exception?.let { ex -> put("exception", ex) }
                metric.dispatcherName?.let { d -> put("dispatcher", d) }
            }
            it.report(payload)
        }
    }
}
