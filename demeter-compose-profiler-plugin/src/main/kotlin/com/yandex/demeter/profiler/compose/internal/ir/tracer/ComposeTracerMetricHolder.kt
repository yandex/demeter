package com.yandex.demeter.profiler.compose.internal.ir.tracer

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Composer
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.compose.internal.ir.tracer.ComposeTracerMetricHolder.log
import com.yandex.demeter.internal.utils.constructorProperties
import com.yandex.demeter.profiler.compose.internal.core.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * Tied to Demeter plugin.
 * Do not change [log] without any change in plugin.
 */
@InternalDemeterApi
object ComposeTracerMetricHolder {
    private const val CURRENT_TRACE_INDEX = 3

    private val initCounter = ConcurrentHashMap<String, Int>()

    private val publisherChannel = Channel<ChannelComposeMetric>(Channel.UNLIMITED)

    private val _initializedMetrics = ConcurrentHashMap<String, ComposeTracerMetric>()
    internal val initializedMetrics: Map<String, ComposeTracerMetric>
        get() = _initializedMetrics.toMap()

    fun init(consumerScope: CoroutineScope) {
        _initializedMetrics.clear()
        initCounter.clear()

        consumerScope.launch {
            for (data in publisherChannel) {
                val simpleName = data.initializedClass.name

                if (!_initializedMetrics.containsKey(simpleName)) {
                    val initializedArgsMetrics = mutableListOf<ComposeTracerMetric>()
                    val originClass = Class.forName(data.className).kotlin
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

                    val initMetric = ComposeTracerMetric(
                        data.initializedClass,
                        initTime = data.took,
                        args = initializedArgsMetrics,
                        threadName = data.threadName
                    )

                    _initializedMetrics[simpleName] = initMetric
                    continue
                }

                val counterVal = initCounter.getOrElse(simpleName) { 1 } + 1
                initCounter[simpleName] = counterVal

                val initMetric = ComposeTracerMetric(
                    cls = data.initializedClass,
                    initTime = data.took,
                    instanceNo = counterVal,
                    threadName = data.threadName
                )

                _initializedMetrics["$simpleName №$counterVal"] = initMetric
            }
        }
    }

    @JvmStatic
    fun log(initializedClass: Class<*>, composer: Composer, name: String, startTime: Long) {
        val took = SystemClock.elapsedRealtime() - startTime
        val thread = Thread.currentThread()
        Log.d(TAG, "Is skipping: ${composer.skipping && !composer.composition.hasPendingChanges}. enter $name, ${initializedClass.name}, ${initializedClass.simpleName}")

        try {
            val trace = thread.stackTrace[CURRENT_TRACE_INDEX]
            if (trace.className.contains("ComposeTracerMetricHolder")) {
                return
            }

            publisherChannel.trySend(
                ChannelComposeMetric(
                    initializedClass,
                    trace.className,
                    startTime,
                    took,
                    thread.name,
                )
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Can't make stacktrace", e)
        }
    }

    private data class ChannelComposeMetric(
        val initializedClass: Class<*>,
        val className: String,
        val startTime: Long,
        val took: Long,
        val threadName: String,
    )
}
