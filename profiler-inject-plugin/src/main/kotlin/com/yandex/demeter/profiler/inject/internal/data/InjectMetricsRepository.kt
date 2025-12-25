package com.yandex.demeter.profiler.inject.internal.data

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.utils.constructorProperties
import com.yandex.demeter.profiler.inject.internal.data.model.AsmInjectMetric
import com.yandex.demeter.profiler.inject.internal.data.model.InjectMetric
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * Contains inject metrics.
 */
@InternalDemeterApi
object InjectMetricsRepository {

    private val initCounter = ConcurrentHashMap<String, Int>()

    private val _initializedMetrics = ConcurrentHashMap<String, InjectMetric>()
    val initializedMetrics: Map<String, InjectMetric>
        get() = _initializedMetrics.toMap()

    fun putMetric(metric: AsmInjectMetric) {
        val handledBefore = _initializedMetrics.containsKey(metric.initializedClass.name)
        if (handledBefore) putExistingMetric(metric) else putNewMetric(metric)
        InjectMetricsReportersNotifier.report(metric)
    }

    private fun putNewMetric(metric: AsmInjectMetric) {
        val simpleName = metric.initializedClass.name

        val initializedArgsMetrics = mutableListOf<InjectMetric>()
        val originClass = Class.forName(metric.className).kotlin
        val args = originClass.constructorProperties.map { it.returnType }

        for (arg in args) {
            val argClassSimpleName = (arg.classifier as? KClass<*>)?.jvmName
            val argMetrics = _initializedMetrics[argClassSimpleName]
            if (argMetrics != null) {
                initializedArgsMetrics.add(argMetrics)
                _initializedMetrics.remove(argClassSimpleName)
            }
        }

        initCounter[simpleName] = 0

        _initializedMetrics[simpleName] = InjectMetric(
            cls = metric.initializedClass,
            initTime = metric.durationMs,
            args = initializedArgsMetrics,
            threadName = metric.threadName
        )
    }

    private fun putExistingMetric(metric: AsmInjectMetric) {
        val simpleName = metric.initializedClass.name

        val counterVal = initCounter.getOrElse(simpleName) { 1 } + 1
        initCounter[simpleName] = counterVal

        _initializedMetrics["$simpleName №$counterVal"] = InjectMetric(
            cls = metric.initializedClass,
            initTime = metric.durationMs,
            instanceNo = counterVal,
            threadName = metric.threadName
        )
    }

    fun clear() {
        _initializedMetrics.clear()
        initCounter.clear()
    }
}
