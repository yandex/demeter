package com.yandex.demeter.profiler.inject.internal.asm

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.inject.internal.data.model.AsmInjectMetric
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

@InternalDemeterApi
abstract class AbstractInjectAsm(
    protected val queue: Channel<AsmInjectMetric>,
) {
    @Suppress("NOTHING_TO_INLINE")
    protected inline fun logInternal(
        startTimeNs: Long,
        className: String,
        initializedClass: Class<*>,
    ) {
        val finishTimeNs = System.nanoTime()
        queue.trySend(
            AsmInjectMetric(
                initializedClass = initializedClass,
                className = className,
                startTimeNs = startTimeNs,
                finishTimeNs = finishTimeNs,
                threadName = Thread.currentThread().name,
            )
        )
    }
}

/**
 * Responsible for initial processing of ASM events.
 * Tied to Gradle Plugin.
 * Modifications of this class presumably require changes in Gradle Plugin.
 */
@InternalDemeterApi
object InjectAsm : AbstractInjectAsm(
    queue = Channel(UNLIMITED),
) {
    val metricsQueue: Flow<AsmInjectMetric> get() = queue.consumeAsFlow()

    @JvmStatic
    fun log(
        startTimeNs: Long,
        className: String,
        initializedClass: Class<*>,
    ) {
        logInternal(
            startTimeNs = startTimeNs,
            className = className,
            initializedClass = initializedClass,
        )
    }
}
