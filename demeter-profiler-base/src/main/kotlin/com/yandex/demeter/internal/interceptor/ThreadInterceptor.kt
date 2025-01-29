package com.yandex.demeter.internal.interceptor

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric

internal const val MAIN_THREAD_NAME = "main"
internal const val ANY_THREAD_NAME = "all"

@InternalDemeterApi
class ThreadInterceptor(override val name: String) : UiInterceptor {
    override fun <T : TimeMetric> intercept(metrics: Collection<T>): List<T> {
        return metrics.filter { metric ->
            if (name.isBlank() || name == ANY_THREAD_NAME) {
                return@filter true
            }

            metric.threadName == name
        }
    }
}
