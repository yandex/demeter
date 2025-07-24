package com.yandex.demeter.profiler.tracer.internal.data.model

import androidx.collection.MutableLongList
import androidx.collection.MutableObjectList
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric
import kotlin.math.max

@InternalDemeterApi
data class TraceMetric(
    val id: String,
    val className: String,
    val methodName: String,
    val startTimes: MutableLongList = MutableLongList(1), // all times
    val durations: MutableLongList = MutableLongList(1), // all durations
    val threadNames: MutableObjectList<String> = MutableObjectList(1), // all thread names
) : TimeMetric {
    val count: Int
        get() = durations.count()

    val maxTime: Long
        get() {
            var maxDuration = 0L
            durations.forEach {
                maxDuration = max(maxDuration, it)
            }
            return maxDuration
        }

    override val simpleName: String by lazy(LazyThreadSafetyMode.NONE) {
        "$className#$methodName"
    }

    override val totalInitTime: Long
        get() = maxTime

    override val threadName: String
        get() = threadNames.last()

    override val args: List<TimeMetric>
        get() = emptyList()
}
