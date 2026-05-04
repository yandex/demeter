package com.yandex.demeter.profiler.coroutine.tracer

import com.yandex.demeter.internal.utils.buildFirefoxProfilerJson
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.CoroutineMetricRawEntity
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.asRawTraceMetric
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.asRawTraceMetrics
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests export of coroutine trace data for the retry pattern:
 *
 * ```
 * retryPattern (main, 300ms)
 *   └─ IO dispatcher (DefaultDispatcher-worker-1, 250ms)
 *        ├─ attempt 1 (DefaultDispatcher-worker-1, 50ms)
 *        ├─ attempt 2 (DefaultDispatcher-worker-1, 100ms)
 *        └─ attempt 3 (DefaultDispatcher-worker-1, 150ms)
 * ```
 */
class CoroutineTracerExportTest {

    private val baseTime = 1000L

    /**
     * Retry pattern entities matching the showcase CoroutineShowCase.retryPattern():
     * root launch on main -> child launch on IO -> 3 attempt launches.
     */
    private val retryPatternEntities = listOf(
        CoroutineMetricRawEntity(
            traceId = 1L,
            parentTraceId = null,
            launchSite = "CoroutineShowCase#retryPattern",
            durationMs = 300,
            startTimeMs = baseTime,
            launchThreadName = "main",
            completionThreadName = "main",
            isCancelled = false,
            exception = null,
            depth = 0,
        ),
        CoroutineMetricRawEntity(
            traceId = 2L,
            parentTraceId = 1L,
            launchSite = "CoroutineShowCase#retryPattern:IO",
            durationMs = 250,
            startTimeMs = baseTime + 10,
            launchThreadName = "DefaultDispatcher-worker-1",
            completionThreadName = "DefaultDispatcher-worker-1",
            isCancelled = false,
            exception = null,
            depth = 1,
        ),
        CoroutineMetricRawEntity(
            traceId = 3L,
            parentTraceId = 2L,
            launchSite = "CoroutineShowCase#retryPattern:attempt",
            durationMs = 50,
            startTimeMs = baseTime + 20,
            launchThreadName = "DefaultDispatcher-worker-1",
            completionThreadName = "DefaultDispatcher-worker-1",
            isCancelled = false,
            exception = null,
            depth = 2,
        ),
        CoroutineMetricRawEntity(
            traceId = 4L,
            parentTraceId = 2L,
            launchSite = "CoroutineShowCase#retryPattern:attempt",
            durationMs = 100,
            startTimeMs = baseTime + 70,
            launchThreadName = "DefaultDispatcher-worker-1",
            completionThreadName = "DefaultDispatcher-worker-1",
            isCancelled = false,
            exception = null,
            depth = 2,
        ),
        CoroutineMetricRawEntity(
            traceId = 5L,
            parentTraceId = 2L,
            launchSite = "CoroutineShowCase#retryPattern:attempt",
            durationMs = 150,
            startTimeMs = baseTime + 170,
            launchThreadName = "DefaultDispatcher-worker-1",
            completionThreadName = "DefaultDispatcher-worker-1",
            isCancelled = false,
            exception = null,
            depth = 2,
        ),
    )

    // region asRawTraceMetric conversion tests

    @Test
    fun `asRawTraceMetric converts fields correctly`() {
        val entity = retryPatternEntities[0]
        val raw = entity.asRawTraceMetric()

        assertEquals("CoroutineShowCase#retryPattern", raw.methodId)
        assertEquals("CoroutineShowCase", raw.className)
        assertEquals("retryPattern", raw.methodName)
        assertEquals("CoroutineShowCase#retryPattern", raw.simpleName)
        assertEquals(300L, raw.durationMs)
        assertEquals(baseTime, raw.startTimeMs)
        assertEquals("main", raw.threadName)
        assertEquals(1L, raw.executionId)
        assertEquals(null, raw.parentExecutionId)
        assertEquals(null, raw.parentMethodId)
        assertEquals(0, raw.depth)
    }

    @Test
    fun `asRawTraceMetric preserves full method name with colon suffix`() {
        val entity = retryPatternEntities[1] // "CoroutineShowCase#retryPattern:IO"
        val raw = entity.asRawTraceMetric()

        assertEquals("CoroutineShowCase#retryPattern:IO", raw.methodId)
        assertEquals("CoroutineShowCase", raw.className)
        assertEquals("retryPattern:IO", raw.methodName)
        assertEquals("CoroutineShowCase#retryPattern:IO", raw.simpleName)
    }

    @Test
    fun `asRawTraceMetrics preserves parent-child chain`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()

        assertEquals(5, rawMetrics.size)

        // Root has no parent
        assertEquals(null, rawMetrics[0].parentExecutionId)

        // IO child points to root
        assertEquals(1L, rawMetrics[1].parentExecutionId)

        // All attempts point to IO child
        assertEquals(2L, rawMetrics[2].parentExecutionId)
        assertEquals(2L, rawMetrics[3].parentExecutionId)
        assertEquals(2L, rawMetrics[4].parentExecutionId)
    }

    // endregion

    // region Firefox Profiler JSON export tests

    @Test
    fun `firefox profiler json uses tracing weight type`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        assertTrue(
            "\"weightType\":\"tracing-ms\"" in json,
            "JSON should contain weightType:tracing-ms for duration display",
        )
        assertTrue(
            "\"weightType\":\"samples\"" !in json,
            "JSON should NOT contain weightType:samples",
        )
    }

    @Test
    fun `firefox profiler json contains weight array with durations`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        // Worker thread: IO(250ms), attempt1(50ms), attempt2(100ms), attempt3(150ms)
        assertTrue(
            "\"weight\":[250.0,50.0,100.0,150.0]" in json,
            "JSON should contain worker thread weight array with correct durations",
        )

        // Main thread: root(300ms)
        assertTrue(
            "\"weight\":[300.0]" in json,
            "JSON should contain main thread weight array with root duration",
        )
    }

    @Test
    fun `firefox profiler json contains both threads`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        assertTrue("\"name\":\"main\"" in json)
        assertTrue("\"name\":\"DefaultDispatcher-worker-1\"" in json)
    }

    @Test
    fun `firefox profiler json has correct sample counts`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        // Main thread: 1 sample (root only)
        // We check by looking for the main thread's samples section
        // The main thread has stack:[0], time:[0.0], length:1
        assertTrue("\"stack\":[0],\"time\":[0.0],\"length\":1" in json, "main thread should have 1 sample")

        // Worker thread: 4 samples
        assertTrue("\"length\":4}" in json, "worker thread should have 4 samples")
    }

    @Test
    fun `firefox profiler json has correct relative timestamps for worker thread`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        // baseTimeMs = 1000, worker metrics start at 1010, 1020, 1070, 1170
        // Relative: 10.0, 20.0, 70.0, 170.0
        assertTrue(
            "\"time\":[10.0,20.0,70.0,170.0]" in json,
            "Worker thread should have correct relative timestamps",
        )
    }

    @Test
    fun `firefox profiler json has correct stack hierarchy`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        // Worker thread stackTable: IO child has null prefix (root on this thread),
        // all 3 attempts have prefix 0 (pointing to IO child)
        assertTrue(
            "\"prefix\":[null,0,0,0]" in json,
            "Worker thread stack prefixes: IO=null, attempts all point to IO (0)",
        )
    }

    @Test
    fun `firefox profiler json string array contains all function names`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        assertTrue("\"CoroutineShowCase#retryPattern\"" in json)
        assertTrue("\"CoroutineShowCase#retryPattern:IO\"" in json)
        assertTrue("\"CoroutineShowCase#retryPattern:attempt\"" in json)
    }

    @Test
    fun `firefox profiler json has product name`() {
        val rawMetrics = retryPatternEntities.asRawTraceMetrics()
        val json = buildFirefoxProfilerJson(rawMetrics, "coroutine-tracer")

        assertTrue("\"product\":\"coroutine-tracer\"" in json)
    }

    // endregion
}
