package com.yandex.demeter.internal.reporter

import com.yandex.demeter.internal.model.TimeMetric

interface MetricsReportersNotifier<T : TimeMetric> {
    fun report(metric: T)
}
