package com.yandex.demeter.profiler.inject.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.model.TimeMetric

@InternalDemeterApi
data class InjectMetric(
    val className: String,
    val initTime: Long = 0,
    val instanceNo: Int = 0,
    override val threadName: String = Thread.currentThread().name,
    override val args: List<InjectMetric> = listOf(),
    val traceElements: List<StackTraceElement> = emptyList()
) : TimeMetric {
    override val totalInitTime: Long by lazy(LazyThreadSafetyMode.NONE) {
        args.fold(0L) { acc, arg -> acc + arg.initTime }
    }

    override val simpleName: String by lazy(LazyThreadSafetyMode.NONE) {
        val simpleClassName = className.substringAfterLast('.')
        if (instanceNo > 0) {
            "$simpleClassName №$instanceNo"
        } else {
            simpleClassName
        }
    }
}