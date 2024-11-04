package com.yandex.demeter.internal.utils

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.model.TimeMetric

@InternalDemeterApi
class AlphabetComparator : Comparator<TimeMetric> {
    override fun compare(o1: TimeMetric, o2: TimeMetric): Int {
        return o1.simpleName.compareTo(o2.simpleName)
    }
}

@InternalDemeterApi
class TimeComparator : Comparator<TimeMetric> {
    override fun compare(o1: TimeMetric, o2: TimeMetric): Int {
        return WarningLevel.getLevel(o2.totalInitTime).threshold.compareTo(
            WarningLevel.getLevel(o1.totalInitTime).threshold
        )
    }
}

@InternalDemeterApi
enum class SortType(
    val comparator: Comparator<TimeMetric>
) {
    ALPHABET(AlphabetComparator()),
    TIME(TimeComparator())
}
