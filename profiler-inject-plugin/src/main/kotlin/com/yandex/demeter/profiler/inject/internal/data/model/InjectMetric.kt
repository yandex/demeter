package com.yandex.demeter.profiler.inject.internal.data.model

import com.yandex.demeter.internal.model.TimeMetric
import java.lang.reflect.Proxy

internal data class InjectMetric(
    val cls: Class<*>,
    val initTime: Long = 0,
    val instanceNo: Int = 0,
    override val threadName: String = Thread.currentThread().name,
    override val args: List<InjectMetric> = listOf(),
    val traceElements: List<StackTraceElement> = Thread.currentThread().stackTrace.asList()
) : TimeMetric {
    override val totalInitTime: Long by lazy(LazyThreadSafetyMode.NONE) {
        args.fold(0L) { acc, arg -> acc + arg.initTime }
    }

    override val simpleName: String by lazy(LazyThreadSafetyMode.NONE) {
        var className: String
        className = if (Proxy.isProxyClass(cls)) {
            val interfaces = cls.interfaces
            if (interfaces.size == 1) {
                interfaces[0].name
            } else {
                listOf(*interfaces).toString()
            }
        } else {
            cls.name
        }
        val dot = className.lastIndexOf('.')
        if (dot != -1) {
            className = className.substring(dot + 1)
        }
        if (instanceNo > 0) {
            "$className №$instanceNo"
        } else {
            className
        }
    }
}
