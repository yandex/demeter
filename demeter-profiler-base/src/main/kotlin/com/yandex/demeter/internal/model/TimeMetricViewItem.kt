package com.yandex.demeter.internal.model

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
data class TimeMetricViewItem(
    override val simpleName: String = "",
    val description: String = "",
    override val totalInitTime: Long = 0,
    override val threadName: String = "",
    override val args: List<TimeMetricViewItem> = emptyList(),
) : TimeMetric
