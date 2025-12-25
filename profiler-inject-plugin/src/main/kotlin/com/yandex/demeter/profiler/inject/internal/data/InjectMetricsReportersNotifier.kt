package com.yandex.demeter.profiler.inject.internal.data

import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.internal.reporter.MetricsReportersNotifier
import com.yandex.demeter.profiler.inject.internal.data.model.AsmInjectMetric

internal object InjectMetricsReportersNotifier : MetricsReportersNotifier<AsmInjectMetric> {
    private const val PAYLOAD_PARAMETERS_COUNT = 3

    private var reporter: DemeterReporter? = null

    fun init(reporter: DemeterReporter) {
        this.reporter = reporter
    }

    override fun report(metric: AsmInjectMetric) {
        reporter?.let {
            val payload = HashMap<String, Any>(PAYLOAD_PARAMETERS_COUNT).apply {
                put("className", metric.simpleName)
                put("ms", metric.totalInitTime)
                put("thread", metric.threadName)
            }
            it.report(payload)
        }
    }
}
