package com.yandex.demeter.profiler.inject.internal.data

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.inject.internal.data.model.AsmInjectMetric
import com.yandex.demeter.profiler.inject.internal.data.model.InjectMetric
import java.util.concurrent.ConcurrentHashMap

/** Contains inject metrics identified by their pre-obfuscation names, without loading application classes. */
@InternalDemeterApi
object InjectMetricsRepository {

    private val initCounter = mutableMapOf<String, Int>()
    private val metrics = linkedMapOf<String, InjectMetric>()
    private val parameterTypesCache = ConcurrentHashMap<String, List<String>>()

    // A null entry means that several unmatched instances exist: a declared type cannot identify one of them.
    private val dependencyCandidates = mutableMapOf<String, String?>()

    val initializedMetrics: Map<String, InjectMetric>
        @Synchronized get() = metrics.toMap()

    fun putMetric(metric: AsmInjectMetric) {
        val parameterTypes = uniqueParameterTypes(metric.parameterClassNames)
        synchronized(this) {
            val instanceNo = (initCounter[metric.className] ?: -1) + 1
            initCounter[metric.className] = instanceNo
            val key = if (instanceNo == 0) metric.className else "${metric.className} №$instanceNo"
            val args = takeDependencies(parameterTypes)

            metrics[key] = InjectMetric(
                className = metric.className,
                initTime = metric.durationMs,
                instanceNo = instanceNo,
                args = args,
                threadName = metric.threadName,
            )
            dependencyCandidates[metric.className] = if (metric.className in dependencyCandidates) null else key
        }
        InjectMetricsReportersNotifier.report(metric)
    }

    private fun uniqueParameterTypes(parameterClassNames: String): List<String> {
        if (parameterClassNames.isEmpty()) return emptyList()
        return parameterTypesCache.getOrPut(parameterClassNames) {
            parameterClassNames.split(';').groupingBy { it }.eachCount()
                .filterValues { it == 1 }.keys.toList()
        }
    }

    private fun takeDependencies(parameterTypes: List<String>): List<InjectMetric> {
        return parameterTypes.mapNotNull { className ->
            // Do not guess implementations, unwrap Provider/Lazy, or reuse one metric for two parameters.
            val key = dependencyCandidates[className]
            if (key != null) {
                dependencyCandidates.remove(className)
                metrics.remove(key)
            } else {
                null
            }
        }
    }

    @Synchronized
    fun clear() {
        initCounter.clear()
        metrics.clear()
        dependencyCandidates.clear()
        parameterTypesCache.clear()
    }
}