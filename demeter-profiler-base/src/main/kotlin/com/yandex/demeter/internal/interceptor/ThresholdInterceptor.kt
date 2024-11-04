package com.yandex.demeter.internal.interceptor

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.model.TimeMetric

@InternalDemeterApi
class ThresholdInterceptor(
    override val name: String,
    private val warningLevel: WarningLevel
) : UiInterceptor {
    override fun <T : TimeMetric> intercept(metrics: Collection<T>): List<T> {
        return metrics.filter { metric ->
            metric.totalInitTime >= warningLevel.threshold
        }
    }
}
