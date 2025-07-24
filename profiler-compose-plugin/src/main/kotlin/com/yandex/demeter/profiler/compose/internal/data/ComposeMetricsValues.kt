package com.yandex.demeter.profiler.compose.internal.data

import androidx.collection.MutableObjectList
import com.yandex.demeter.profiler.compose.internal.data.model.ComposeMetric

internal object ComposeMetricsValues {
    private const val INITIAL_CAPACITY = 1 shl 14

    val composeMetrics = MutableObjectList<ComposeMetric>(INITIAL_CAPACITY)
    val composeMetricsAsList: List<ComposeMetric>
        get() = composeMetrics.asList()
}
