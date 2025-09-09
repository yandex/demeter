package com.yandex.demeter.profiler.tracer.internal.asm

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

@InternalDemeterApi
abstract class AbstractTracerAsm(
    protected val queue: Channel<AsmTraceMetric>,
    protected val systemTraceBufferBeginSection: (Long, String) -> Unit,
    protected val systemTraceBufferEndSection: (Long, String) -> Unit
) {

    protected inline fun beginSectionInternal(
        startTimeNs: Long,
        fullMethodName: String,
    ) {
        systemTraceBufferBeginSection(startTimeNs, fullMethodName)
    }

    protected inline fun endSectionInternal(
        startTimeNs: Long,
        fullMethodName: String,
        className: String,
        methodName: String,
    ) {
        val finishTimeNs = System.nanoTime()
        systemTraceBufferEndSection(startTimeNs, fullMethodName)

        queue.trySend(
            AsmTraceMetric(
                id = fullMethodName,
                className = className,
                methodName = methodName,
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
object TracerAsm : AbstractTracerAsm(
    queue = Channel(UNLIMITED),
    systemTraceBufferBeginSection = SystemTraceBuffer::beginSection,
    systemTraceBufferEndSection = SystemTraceBuffer::endSection,
) {
    internal val metricsQueue: Flow<AsmTraceMetric> get() = queue.consumeAsFlow()

    @JvmStatic
    fun beginSection(
        startTimeNs: Long,
        fullMethodName: String,
    ) {
        if (EnabledValueHolder.isEnabled) {
            beginSectionInternal(
                startTimeNs = startTimeNs,
                fullMethodName = fullMethodName,
            )
        }

    }

    @JvmStatic
    fun endSection(
        startTimeNs: Long,
        fullMethodName: String,
        className: String,
        methodName: String,
    ) {
        if (EnabledValueHolder.isEnabled) {
            endSectionInternal(
                startTimeNs = startTimeNs,
                fullMethodName = fullMethodName,
                className = className,
                methodName = methodName,
            )
        }

    }
}

object EnabledValueHolder {
    var isEnabled = false
}