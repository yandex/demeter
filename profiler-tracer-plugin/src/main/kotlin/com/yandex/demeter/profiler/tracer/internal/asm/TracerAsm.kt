package com.yandex.demeter.profiler.tracer.internal.asm

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicLong

/**
 * Maximum channel capacity to prevent unbounded memory growth
 */
private const val CHANNEL_CAPACITY = 10_000

/**
 * Minimum duration in nanoseconds to record (filters out very fast methods)
 */
private const val MIN_DURATION_NS = 100_000L // 0.1ms

@InternalDemeterApi
data class CallFrame(
    val methodId: String,
    val startTimeNs: Long,
    val executionId: Long,
)

private object TracerState {
    val callStack: ThreadLocal<ArrayDeque<CallFrame>> = ThreadLocal()
    val executionIdGenerator = AtomicLong(0)
    val queue: Channel<AsmTraceMetric> = Channel(CHANNEL_CAPACITY)
}

/**
 * Responsible for initial processing of ASM events.
 * Tied to Gradle Plugin.
 * Modifications of this class presumably require changes in Gradle Plugin.
 */
@InternalDemeterApi
object TracerAsm {
    internal val metricsQueue: Flow<AsmTraceMetric> get() = TracerState.queue.consumeAsFlow()

    private fun getOrCreateStack(): ArrayDeque<CallFrame> {
        var stack = TracerState.callStack.get()
        if (stack == null) {
            stack = ArrayDeque()
            TracerState.callStack.set(stack)
        }
        return stack
    }

    @JvmStatic
    fun beginSection(
        startTimeNs: Long,
        fullMethodName: String,
    ) {
        SystemTraceBuffer.beginSection(startTimeNs, fullMethodName)

        val stack = getOrCreateStack()
        val executionId = TracerState.executionIdGenerator.incrementAndGet()
        stack.push(CallFrame(fullMethodName, startTimeNs, executionId))
    }

    @JvmStatic
    fun endSection(
        startTimeNs: Long,
        fullMethodName: String,
        className: String,
        methodName: String,
    ) {
        val finishTimeNs = System.nanoTime()
        SystemTraceBuffer.endSection(startTimeNs, fullMethodName)

        val stack = getOrCreateStack()
        val currentFrame = if (stack.isNotEmpty()) stack.pop() else null
        val parentFrame = if (stack.isNotEmpty()) stack.peek() else null

        // Filter out very fast methods to reduce noise
        val durationNs = finishTimeNs - startTimeNs
        if (durationNs < MIN_DURATION_NS) {
            return
        }

        TracerState.queue.trySend(
            AsmTraceMetric(
                id = fullMethodName,
                className = className,
                methodName = methodName,
                startTimeNs = startTimeNs,
                finishTimeNs = finishTimeNs,
                threadName = Thread.currentThread().name,
                executionId = currentFrame?.executionId ?: TracerState.executionIdGenerator.incrementAndGet(),
                parentExecutionId = parentFrame?.executionId,
                parentMethodId = parentFrame?.methodId,
                depth = stack.size,
            )
        )
    }
}
