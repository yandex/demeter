package com.yandex.demeter.profiler.compose.internal.data

import android.os.SystemClock
import android.util.Log
import com.yandex.demeter.profiler.compose.internal.data.model.ComposeMetric.Changed
import com.yandex.demeter.profiler.compose.internal.data.model.ComposeMetric.Forgotten
import com.yandex.demeter.profiler.compose.internal.data.model.ComposeMetric.Remembered
import com.yandex.demeter.profiler.compose.internal.ir.recompositions.ObjectRecomposition
import com.yandex.demeter.profiler.compose.internal.ir.recompositions.RecompositionNotifier
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectChange
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectChangeNotifier
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectComposition
import com.yandex.demeter.profiler.compose.internal.data.model.ComposeMetric
import com.yandex.demeter.profiler.compose.internal.core.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal object ComposeMetricHolder {
    val stateObjectNotifier = object : StateObjectChangeNotifier {
        override fun changed(composition: StateObjectComposition, change: StateObjectChange) {
            Log.i(TAG, "[Change] $composition: $change")
            val timestamp = SystemClock.elapsedRealtime()
            publisherChannel.trySend(
                Changed(timestamp, composition, change)
            )
        }

        override fun forgotten(composition: StateObjectComposition) {
            Log.i(TAG, "[Forgotten] $composition")
            val timestamp = SystemClock.elapsedRealtime()
            publisherChannel.trySend(
                Forgotten(timestamp, composition)
            )
        }

        override fun remembered(composition: StateObjectComposition, change: StateObjectChange) {
            Log.i(TAG, "[Remembered] $composition: $change")
            val timestamp = SystemClock.elapsedRealtime()
            publisherChannel.trySend(
                Remembered(timestamp, composition, change)
            )
        }
    }

    val recompositionsNotifier = object : RecompositionNotifier {
        override fun recomposed(recomposition: ObjectRecomposition) {
            Log.i(TAG,"[Recomposed] $recomposition")
            val timestamp = SystemClock.elapsedRealtime()
            publisherChannel.trySend(
                ComposeMetric.Recomposed(timestamp, recomposition)
            )
        }

        override fun skipped(recomposition: ObjectRecomposition) {
            Log.i(TAG,"[Skipped] $recomposition")
            val timestamp = SystemClock.elapsedRealtime()
            publisherChannel.trySend(
                ComposeMetric.Recomposed(timestamp, recomposition)
            )
        }
    }

    private val publisherChannel = Channel<ComposeMetric>(Channel.UNLIMITED)

    fun init(consumerScope: CoroutineScope) {
        consumerScope.launch {
            for (data in publisherChannel) {
                ComposeMetricsValues.composeMetrics.add(data)
            }
        }
    }
}
