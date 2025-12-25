package com.yandex.demeter.profiler.compose.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.compose.internal.ir.recompositions.ObjectRecomposition
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectChange
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectComposition

@InternalDemeterApi
sealed interface ComposeMetric {
    val timestamp: Long

    data class Changed(
        override val timestamp: Long,
        val composition: StateObjectComposition,
        val change: StateObjectChange,
    ) : ComposeMetric

    data class Remembered(
        override val timestamp: Long,
        val composition: StateObjectComposition,
        val change: StateObjectChange
    ) : ComposeMetric

    data class Forgotten(
        override val timestamp: Long,
        val composition: StateObjectComposition
    ) : ComposeMetric

    data class Recomposed(
        override val timestamp: Long,
        val recomposition: ObjectRecomposition,
    ) : ComposeMetric

    data class Skipped(
        override val timestamp: Long,
        val recomposition: ObjectRecomposition,
    ) : ComposeMetric
}
