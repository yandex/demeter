@file:OptIn(InternalDemeterApi::class)

package com.yandex.demeter.profiler.coroutine.tracer

import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.coroutine.tracer.internal.asm.CoroutineTracerAsm
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.AsmCoroutineMetric
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [CoroutineTracerAsm] runtime hook logic.
 *
 * [CoroutineTracerAsm] is a singleton with a shared internal channel exposed via
 * [CoroutineTracerAsm.metricsQueue]. A single class-scoped collector keeps the test
 * suite simple and avoids re-collecting the same flow per test method.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoroutineTracerAsmTest {

    private val collectedMetrics = mutableListOf<AsmCoroutineMetric>()
    private val collectorScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    init {
        collectorScope.launch {
            CoroutineTracerAsm.metricsQueue.collect { collectedMetrics.add(it) }
        }
    }

    @AfterAll
    fun tearDown() {
        collectorScope.cancel()
    }

    private fun drainMetricsForSite(launchSite: String): List<AsmCoroutineMetric> {
        return collectedMetrics.filter { it.launchSite == launchSite }
    }

    @Test
    fun `onCoroutineLaunched sends metric on job completion`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val site = "AsmTest#completion:${System.nanoTime()}"

        val job = scope.launch { }
        CoroutineTracerAsm.onCoroutineLaunched(job, site)
        advanceUntilIdle()

        val metrics = drainMetricsForSite(site)
        assertEquals(1, metrics.size)
        assertFalse(metrics[0].isCancelled)
        assertNull(metrics[0].exception)
        assertTrue(metrics[0].durationMs >= 0)

        scope.cancel()
    }

    @Test
    fun `cancelled job is tracked with isCancelled flag`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val site = "AsmTest#cancelled:${System.nanoTime()}"

        val job = scope.launch { delay(10_000) }
        CoroutineTracerAsm.onCoroutineLaunched(job, site)
        job.cancel()
        advanceUntilIdle()

        val metrics = drainMetricsForSite(site)
        assertEquals(1, metrics.size)
        assertTrue(metrics[0].isCancelled)
        assertNull(metrics[0].exception)

        scope.cancel()
    }

    @Test
    fun `exception in coroutine is recorded`() = runTest {
        val handler = CoroutineExceptionHandler { _, _ -> }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined + handler)
        val site = "AsmTest#exception:${System.nanoTime()}"

        val job = scope.launch {
            throw IllegalStateException("test error")
        }
        CoroutineTracerAsm.onCoroutineLaunched(job, site)
        advanceUntilIdle()

        val metrics = drainMetricsForSite(site)
        assertEquals(1, metrics.size)
        assertFalse(metrics[0].isCancelled)
        assertNotNull(metrics[0].exception)
        assertTrue(metrics[0].exception!!.contains("IllegalStateException"))
        assertTrue(metrics[0].exception!!.contains("test error"))

        scope.cancel()
    }

    @Test
    fun `parent-child relationship detected via Job hierarchy`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val parentSite = "AsmTest#parent:${System.nanoTime()}"
        val childSite = "AsmTest#child:${System.nanoTime()}"

        scope.launch {
            val parentJob = coroutineContext[Job]!!
            CoroutineTracerAsm.onCoroutineLaunched(parentJob, parentSite)

            launch {
                val childJob = coroutineContext[Job]!!
                CoroutineTracerAsm.onCoroutineLaunched(childJob, childSite)
            }
        }
        advanceUntilIdle()

        val parent = drainMetricsForSite(parentSite).firstOrNull()
        val child = drainMetricsForSite(childSite).firstOrNull()

        assertNotNull(parent)
        assertNotNull(child)
        assertNull(parent.parentTraceId)
        assertEquals(parent.traceId, child.parentTraceId)
        assertEquals(0, parent.depth)
        assertEquals(1, child.depth)

        scope.cancel()
    }

    @Test
    fun `thread name is captured at launch site`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val site = "AsmTest#thread:${System.nanoTime()}"
        val currentThread = Thread.currentThread().name

        val job = scope.launch { }
        CoroutineTracerAsm.onCoroutineLaunched(job, site)
        advanceUntilIdle()

        val metrics = drainMetricsForSite(site)
        assertEquals(1, metrics.size)
        assertEquals(currentThread, metrics[0].launchThreadName)

        scope.cancel()
    }

    @Test
    fun `traceId is unique across multiple calls`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val site1 = "AsmTest#unique1:${System.nanoTime()}"
        val site2 = "AsmTest#unique2:${System.nanoTime()}"

        val job1 = scope.launch { }
        CoroutineTracerAsm.onCoroutineLaunched(job1, site1)
        val job2 = scope.launch { }
        CoroutineTracerAsm.onCoroutineLaunched(job2, site2)
        advanceUntilIdle()

        val metric1 = drainMetricsForSite(site1).firstOrNull()
        val metric2 = drainMetricsForSite(site2).firstOrNull()
        assertNotNull(metric1)
        assertNotNull(metric2)
        assertTrue(metric1.traceId != metric2.traceId)

        scope.cancel()
    }
}
