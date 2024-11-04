package com.yandex.demeter.profiler.compose.internal.ir.tracer

import com.yandex.demeter.internal.model.TimeMetric
import java.lang.reflect.Proxy

internal data class ComposeTracerMetric(
    val cls: Class<*>,
    val initTime: Long = 0,
    val instanceNo: Int = 0,
    override val threadName: String = Thread.currentThread().name,
    override val args: List<ComposeTracerMetric> = listOf(),
    val traceElements: List<StackTraceElement> = Thread.currentThread().stackTrace.asList()
) : TimeMetric {

    override val totalInitTime: Long
        get() {
            var total = initTime
            for (initMetric in args) {
                total += initMetric.totalInitTime
            }
            return total
        }

    override val simpleName: String
        get() {
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
            return if (instanceNo > 0) {
                "$className №$instanceNo"
            } else {
                className
            }
        }
}
