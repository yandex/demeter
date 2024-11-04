package com.yandex.demeter.internal.model

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
interface TimeMetric {
    val totalInitTime: Long
    val simpleName: String
    val threadName: String
    val args: List<TimeMetric>
}
