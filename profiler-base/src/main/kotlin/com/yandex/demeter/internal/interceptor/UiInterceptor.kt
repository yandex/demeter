package com.yandex.demeter.internal.interceptor

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric

@InternalDemeterApi
interface UiInterceptor {
    val name: String
    fun <T : TimeMetric> intercept(metrics: Collection<T>): List<T>
}
