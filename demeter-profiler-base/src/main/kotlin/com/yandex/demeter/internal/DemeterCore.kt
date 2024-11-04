package com.yandex.demeter.internal

import com.yandex.demeter.Demeter
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel.Zero
import com.yandex.demeter.internal.interceptor.ThreadInterceptor
import com.yandex.demeter.internal.interceptor.ThresholdInterceptor

@InternalDemeterApi
class DemeterCore : Demeter.Core {
    val mainInterceptor = ThresholdInterceptor("All events", Zero)

    val interceptors: List<ThresholdInterceptor> = listOf(
        mainInterceptor,
        ThresholdInterceptor("Warning ms > ${WarningLevel.First.threshold}", WarningLevel.First),
        ThresholdInterceptor("Warning ms > ${WarningLevel.Second.threshold}", WarningLevel.Second),
        ThresholdInterceptor("Warning ms > ${WarningLevel.Third.threshold}", WarningLevel.Third)
    )

    val threadsFilters: List<ThreadInterceptor> = listOf(
        ThreadInterceptor("main thread"),
        ThreadInterceptor("all threads")
    )
}
