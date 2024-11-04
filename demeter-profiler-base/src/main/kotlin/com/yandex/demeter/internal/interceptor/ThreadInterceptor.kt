package com.yandex.demeter.internal.interceptor

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric

@InternalDemeterApi
class ThreadInterceptor(override val name: String) : UiInterceptor {
    override fun <T : TimeMetric> intercept(metrics: Collection<T>): List<T> {
        return metrics.filter { metric ->
            if (name == "") {
                return@filter true
            }

            metric.threadName == name
        }
    }
}
