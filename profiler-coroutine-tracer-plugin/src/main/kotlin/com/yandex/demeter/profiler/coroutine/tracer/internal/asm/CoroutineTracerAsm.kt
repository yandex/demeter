package com.yandex.demeter.profiler.coroutine.tracer.internal.asm

import android.util.Log
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.coroutine.tracer.internal.core.TAG
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.AsmCoroutineMetric
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.cancellation.CancellationException

/**
 * Maximum channel capacity to prevent unbounded memory growth
 */
private const val CHANNEL_CAPACITY = 10_000

@InternalDemeterApi
private data class CoroutineTraceInfo(
    val traceId: Long,
    val parentTraceId: Long?,
    val job: Job,
    val launchSite: String,
    val startTimeNs: Long,
    val threadName: String,
    val depth: Int,
    val dispatcherName: String?,
)

/**
 * Responsible for initial processing of coroutine lifecycle ASM events.
 * Tied to Gradle Plugin.
 * Modifications of this class presumably require changes in Gradle Plugin.
 */
@InternalDemeterApi
object CoroutineTracerAsm {
    private val traceIdGenerator = AtomicLong(0)
    private val queue: Channel<AsmCoroutineMetric> = Channel(CHANNEL_CAPACITY)
    internal val metricsQueue: Flow<AsmCoroutineMetric> get() = queue.receiveAsFlow()

    // Active coroutines being tracked (traceId -> start info)
    private val activeCoroutines = ConcurrentHashMap<Long, CoroutineTraceInfo>()
    // Reverse index: Job identity -> traceId, used to bound parent search to active jobs only.
    private val jobToTraceId = ConcurrentHashMap<Job, Long>()

    private val droppedCount = AtomicLong(0)

    /**
     * Number of metric events dropped because the internal channel was full or closed.
     */
    val droppedMetricsCount: Long get() = droppedCount.get()

    /**
     * Called by ASM-instrumented code AFTER a coroutine builder (launch/async) returns a Job.
     * The Job is on the stack, we DUP it and call this method.
     */
    @JvmStatic
    fun onCoroutineLaunched(job: Job, launchSite: String) {
        val traceId = traceIdGenerator.incrementAndGet()
        val startTimeNs = System.nanoTime()
        val threadName = Thread.currentThread().name

        val parentTraceId = findParentTraceId(job)
        val depth = if (parentTraceId != null) {
            (activeCoroutines[parentTraceId]?.depth ?: -1) + 1
        } else {
            0
        }

        // AbstractCoroutine in kotlinx-coroutines implements both Job and CoroutineScope,
        // which lets us reach the dispatcher via coroutineContext. This is an internal
        // implementation detail but stable across kotlinx-coroutines releases.
        val dispatcherName = (job as? CoroutineScope)
            ?.coroutineContext
            ?.get(ContinuationInterceptor)
            ?.toString()

        val info = CoroutineTraceInfo(
            traceId = traceId,
            parentTraceId = parentTraceId,
            job = job,
            launchSite = launchSite,
            startTimeNs = startTimeNs,
            threadName = threadName,
            depth = depth,
            dispatcherName = dispatcherName,
        )
        activeCoroutines[traceId] = info
        jobToTraceId[job] = traceId

        // Register completion callback
        job.invokeOnCompletion { cause ->
            val endTimeNs = System.nanoTime()
            activeCoroutines.remove(traceId)
            jobToTraceId.remove(job)

            val isCancelled = cause is CancellationException
            val exceptionName = cause?.takeIf { it !is CancellationException }
                ?.let { "${it::class.simpleName}: ${it.message}" }

            queue.trySend(
                AsmCoroutineMetric(
                    traceId = traceId,
                    parentTraceId = parentTraceId,
                    launchSite = launchSite,
                    startTimeNs = startTimeNs,
                    endTimeNs = endTimeNs,
                    launchThreadName = threadName,
                    completionThreadName = Thread.currentThread().name,
                    isCancelled = isCancelled,
                    exception = exceptionName,
                    depth = depth,
                    dispatcherName = dispatcherName,
                )
            ).onFailure {
                val dropped = droppedCount.incrementAndGet()
                Log.w(TAG, "Coroutine metric dropped (channel full or closed). Total dropped: $dropped")
            }
        }
    }

    private fun findParentTraceId(childJob: Job): Long? {
        // kotlinx-coroutines exposes Job.children but not Job.parent in public API,
        // so we scan active jobs for one whose children reference childJob by identity.
        return jobToTraceId.entries.firstOrNull { (parentJob, _) ->
            parentJob.children.any { it === childJob }
        }?.value
    }
}
