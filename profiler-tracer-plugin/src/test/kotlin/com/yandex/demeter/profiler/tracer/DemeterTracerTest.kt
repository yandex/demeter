package com.yandex.demeter.profiler.tracer

import androidx.paging.PagingData
import com.yandex.demeter.internal.utils.SortType
import com.yandex.demeter.profiler.tracer.internal.asm.CallFrame
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsRepository
import com.yandex.demeter.profiler.tracer.internal.data.db.MethodStatsEntity
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricEntity
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricRawEntity
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TEST_CLASS_NAME = "TEST_CLASS_NAME"
private const val TEST_METHOD_NAME = "TEST_METHOD_NAME"
private const val TEST_ID = "$TEST_CLASS_NAME#$TEST_METHOD_NAME"

class DemeterTracerTest {

    private lateinit var testRepository: TestTraceMetricsRepository

    @BeforeTest
    fun setup() {
        testRepository = TestTraceMetricsRepository()
    }

    @Test
    fun `calculate max time for one event`() = runTest {
        val metric1 = createAsmMetric(durationMs = 100)
        val metric2 = createAsmMetric(durationMs = 300)

        testRepository.upsertMetric(metric1)
        testRepository.upsertMetric(metric2)

        assertEquals(1, testRepository.metrics.size)
        val metric = testRepository.metrics.values.first()
        assertEquals(2, metric.count)
        assertEquals(300, metric.maxDurationMs)
    }

    @Test
    fun `collect two different events`() = runTest {
        val metric1 = createAsmMetric(
            id = "Class1#method1",
            className = "Class1",
            methodName = "method1",
            durationMs = 100
        )
        val metric2 = createAsmMetric(
            id = "Class2#method2",
            className = "Class2",
            methodName = "method2",
            durationMs = 500
        )

        testRepository.upsertMetric(metric1)
        testRepository.upsertMetric(metric2)

        assertEquals(2, testRepository.metrics.size)
        val metric100 = testRepository.metrics.values.first { it.maxDurationMs == 100L }
        val metric500 = testRepository.metrics.values.first { it.maxDurationMs == 500L }

        assertEquals(1, metric100.count)
        assertEquals(100, metric100.maxDurationMs)
        assertEquals(1, metric500.count)
        assertEquals(500, metric500.maxDurationMs)
    }

    @Test
    fun `get call stack for execution returns correct hierarchy`() = runTest {
        // Create a call stack: root -> child -> grandchild
        val rootExecId = 1L
        val childExecId = 2L
        val grandchildExecId = 3L
        val baseTime = System.currentTimeMillis()

        val root = createAsmMetric(
            id = "Root#method",
            className = "Root",
            methodName = "method",
            startTimeMs = baseTime,
            durationMs = 300,
            executionId = rootExecId,
            parentExecutionId = null,
            parentMethodId = null,
            depth = 0
        )
        val child = createAsmMetric(
            id = "Child#method",
            className = "Child",
            methodName = "method",
            startTimeMs = baseTime + 10,
            durationMs = 200,
            executionId = childExecId,
            parentExecutionId = rootExecId,
            parentMethodId = "Root#method",
            depth = 1
        )
        val grandchild = createAsmMetric(
            id = "Grandchild#method",
            className = "Grandchild",
            methodName = "method",
            startTimeMs = baseTime + 20,
            durationMs = 100,
            executionId = grandchildExecId,
            parentExecutionId = childExecId,
            parentMethodId = "Child#method",
            depth = 2
        )

        testRepository.upsertMetric(root)
        testRepository.upsertMetric(child)
        testRepository.upsertMetric(grandchild)

        // Get call stack from grandchild - should return all 3 in order
        val callStack = testRepository.getCallStackForExecution(grandchildExecId)

        assertEquals(3, callStack.size)
        assertEquals(0, callStack[0].depth)
        assertEquals(1, callStack[1].depth)
        assertEquals(2, callStack[2].depth)
        assertEquals("Root", callStack[0].className)
        assertEquals("Child", callStack[1].className)
        assertEquals("Grandchild", callStack[2].className)
    }

    @Test
    fun `get call tree returns all descendants`() = runTest {
        val rootExecId = 10L
        val child1ExecId = 11L
        val child2ExecId = 12L
        val grandchildExecId = 13L
        val baseTime = System.currentTimeMillis()

        val root = createAsmMetric(
            id = "Root#main",
            className = "Root",
            methodName = "main",
            startTimeMs = baseTime,
            durationMs = 500,
            executionId = rootExecId,
            depth = 0
        )
        val child1 = createAsmMetric(
            id = "Service#process",
            className = "Service",
            methodName = "process",
            startTimeMs = baseTime + 5,
            durationMs = 200,
            executionId = child1ExecId,
            parentExecutionId = rootExecId,
            parentMethodId = "Root#main",
            depth = 1
        )
        val child2 = createAsmMetric(
            id = "Service#validate",
            className = "Service",
            methodName = "validate",
            startTimeMs = baseTime + 210,
            durationMs = 150,
            executionId = child2ExecId,
            parentExecutionId = rootExecId,
            parentMethodId = "Root#main",
            depth = 1
        )
        val grandchild = createAsmMetric(
            id = "Dao#query",
            className = "Dao",
            methodName = "query",
            startTimeMs = baseTime + 10,
            durationMs = 100,
            executionId = grandchildExecId,
            parentExecutionId = child1ExecId,
            parentMethodId = "Service#process",
            depth = 2
        )

        testRepository.upsertMetric(root)
        testRepository.upsertMetric(child1)
        testRepository.upsertMetric(child2)
        testRepository.upsertMetric(grandchild)

        val tree = testRepository.getCallTree(rootExecId)

        assertEquals(4, tree.size)
        // Should be ordered by depth, then by startTimeMs
        assertEquals("Root", tree[0].className)
        assertEquals(0, tree[0].depth)
    }

    @Test
    fun `get child calls returns direct children only`() = runTest {
        val parentExecId = 100L
        val child1ExecId = 101L
        val child2ExecId = 102L
        val grandchildExecId = 103L
        val baseTime = System.currentTimeMillis()

        val parent = createAsmMetric(
            id = "Parent#method",
            className = "Parent",
            methodName = "method",
            executionId = parentExecId,
            startTimeMs = baseTime,
            durationMs = 500,
            depth = 0
        )
        val child1 = createAsmMetric(
            id = "Child1#method",
            className = "Child1",
            methodName = "method",
            executionId = child1ExecId,
            parentExecutionId = parentExecId,
            parentMethodId = "Parent#method",
            startTimeMs = baseTime + 100,
            durationMs = 50,
            depth = 1
        )
        val child2 = createAsmMetric(
            id = "Child2#method",
            className = "Child2",
            methodName = "method",
            executionId = child2ExecId,
            parentExecutionId = parentExecId,
            parentMethodId = "Parent#method",
            startTimeMs = baseTime + 10,
            durationMs = 80,
            depth = 1
        )
        val grandchild = createAsmMetric(
            id = "Grandchild#method",
            className = "Grandchild",
            methodName = "method",
            executionId = grandchildExecId,
            parentExecutionId = child1ExecId,
            parentMethodId = "Child1#method",
            startTimeMs = baseTime + 110,
            durationMs = 20,
            depth = 2
        )

        testRepository.upsertMetric(parent)
        testRepository.upsertMetric(child1)
        testRepository.upsertMetric(child2)
        testRepository.upsertMetric(grandchild)

        val children = testRepository.getChildCalls(parentExecId)

        assertEquals(2, children.size)
        assertEquals("Child2", children[0].className)
        assertEquals("Child1", children[1].className)
        assertTrue(children.none { it.className == "Grandchild" })
    }

    @Test
    fun `get slow root calls filters by threshold and depth`() = runTest {
        val baseTime = System.currentTimeMillis()

        val fastRoot = createAsmMetric(
            id = "Fast#method",
            className = "Fast",
            methodName = "method",
            executionId = 1L,
            startTimeMs = baseTime,
            durationMs = 50,
            depth = 0
        )
        val slowRoot = createAsmMetric(
            id = "Slow#method",
            className = "Slow",
            methodName = "method",
            executionId = 2L,
            startTimeMs = baseTime + 100,
            durationMs = 500,
            depth = 0
        )
        val slowChild = createAsmMetric(
            id = "SlowChild#method",
            className = "SlowChild",
            methodName = "method",
            executionId = 3L,
            parentExecutionId = 2L,
            parentMethodId = "Slow#method",
            startTimeMs = baseTime + 110,
            durationMs = 400,
            depth = 1
        )

        testRepository.upsertMetric(fastRoot)
        testRepository.upsertMetric(slowRoot)
        testRepository.upsertMetric(slowChild)

        val slowRoots = testRepository.getSlowRootCalls(thresholdMs = 100)

        assertEquals(1, slowRoots.size)
        assertEquals("Slow", slowRoots[0].className)
        assertEquals(0, slowRoots[0].depth)
    }

    @Test
    fun `get calls in time range returns correct metrics`() = runTest {
        val baseTime = 1000L

        val before = createAsmMetric(
            id = "Before#method",
            className = "Before",
            methodName = "method",
            executionId = 1L,
            startTimeMs = baseTime - 100,
            durationMs = 50,
            depth = 0
        )
        val inRange1 = createAsmMetric(
            id = "InRange1#method",
            className = "InRange1",
            methodName = "method",
            executionId = 2L,
            startTimeMs = baseTime + 50,
            durationMs = 50,
            depth = 0
        )
        val inRange2 = createAsmMetric(
            id = "InRange2#method",
            className = "InRange2",
            methodName = "method",
            executionId = 3L,
            startTimeMs = baseTime + 100,
            durationMs = 50,
            depth = 0
        )
        val after = createAsmMetric(
            id = "After#method",
            className = "After",
            methodName = "method",
            executionId = 4L,
            startTimeMs = baseTime + 300,
            durationMs = 50,
            depth = 0
        )

        testRepository.upsertMetric(before)
        testRepository.upsertMetric(inRange1)
        testRepository.upsertMetric(inRange2)
        testRepository.upsertMetric(after)

        val inRange = testRepository.getCallsInTimeRange(baseTime, baseTime + 200)

        assertEquals(2, inRange.size)
        assertTrue(inRange.all { it.startTimeMs in baseTime..(baseTime + 200) })
    }

    @Test
    fun `get method stats aggregates correctly`() = runTest {
        val baseTime = System.currentTimeMillis()
        val methodId = "Service#process"

        // Multiple calls to the same method with different durations and depths
        val call1 = createAsmMetric(
            id = methodId,
            className = "Service",
            methodName = "process",
            executionId = 1L,
            startTimeMs = baseTime,
            durationMs = 100,
            depth = 0
        )
        val call2 = createAsmMetric(
            id = methodId,
            className = "Service",
            methodName = "process",
            executionId = 2L,
            startTimeMs = baseTime + 200,
            durationMs = 300,
            depth = 1
        )
        val call3 = createAsmMetric(
            id = methodId,
            className = "Service",
            methodName = "process",
            executionId = 3L,
            startTimeMs = baseTime + 600,
            durationMs = 200,
            depth = 2
        )

        testRepository.upsertMetric(call1)
        testRepository.upsertMetric(call2)
        testRepository.upsertMetric(call3)

        val stats = testRepository.getMethodStats()

        assertEquals(1, stats.size)
        val stat = stats[0]
        assertEquals(methodId, stat.methodId)
        assertEquals(3, stat.callCount)
        assertEquals(300L, stat.maxDurationMs)
        assertEquals(100L, stat.minDurationMs)
        assertEquals(200.0, stat.avgDurationMs, 0.01)
        assertEquals(2, stat.maxDepth)
        assertEquals(1.0, stat.avgDepth, 0.01)
    }

    companion object {
        private val executionIdCounter = AtomicLong(0)
    }

    private fun createAsmMetric(
        id: String = TEST_ID,
        className: String = TEST_CLASS_NAME,
        methodName: String = TEST_METHOD_NAME,
        startTimeMs: Long = System.currentTimeMillis(),
        durationMs: Long = 100,
        executionId: Long = executionIdCounter.incrementAndGet(),
        parentExecutionId: Long? = null,
        parentMethodId: String? = null,
        depth: Int = 0,
    ): AsmTraceMetric {
        val startTimeNs = startTimeMs * 1_000_000
        val finishTimeNs = startTimeNs + (durationMs * 1_000_000)
        return AsmTraceMetric(
            id = id,
            className = className,
            methodName = methodName,
            startTimeNs = startTimeNs,
            finishTimeNs = finishTimeNs,
            threadName = "test-thread",
            executionId = executionId,
            parentExecutionId = parentExecutionId,
            parentMethodId = parentMethodId,
            depth = depth,
        )
    }
}

class CallFrameTest {

    @Test
    fun `CallFrame stores correct values`() {
        val frame = CallFrame(
            methodId = "com.example.Service#process",
            startTimeNs = 1234567890L,
            executionId = 42L
        )

        assertEquals("com.example.Service#process", frame.methodId)
        assertEquals(1234567890L, frame.startTimeNs)
        assertEquals(42L, frame.executionId)
    }

    @Test
    fun `CallFrame data class equality works correctly`() {
        val frame1 = CallFrame("Method#a", 100L, 1L)
        val frame2 = CallFrame("Method#a", 100L, 1L)
        val frame3 = CallFrame("Method#b", 100L, 1L)

        assertEquals(frame1, frame2)
        assertTrue(frame1 != frame3)
    }

    @Test
    fun `CallFrame copy creates correct copy with modifications`() {
        val original = CallFrame("Method#original", 100L, 1L)
        val copied = original.copy(executionId = 2L)

        assertEquals("Method#original", copied.methodId)
        assertEquals(100L, copied.startTimeNs)
        assertEquals(2L, copied.executionId)
    }
}

class MethodStatsEntityTest {

    @Test
    fun `simpleName returns correct format`() {
        val stats = MethodStatsEntity(
            methodId = "com.example.Service#process",
            className = "com.example.Service",
            methodName = "process",
            callCount = 10,
            avgDurationMs = 150.5,
            maxDurationMs = 300,
            minDurationMs = 50,
            avgDepth = 2.5,
            maxDepth = 5
        )

        assertEquals("com.example.Service#process", stats.simpleName)
    }

    @Test
    fun `MethodStatsEntity stores all values correctly`() {
        val stats = MethodStatsEntity(
            methodId = "Test#method",
            className = "Test",
            methodName = "method",
            callCount = 100,
            avgDurationMs = 25.5,
            maxDurationMs = 500,
            minDurationMs = 5,
            avgDepth = 3.2,
            maxDepth = 10
        )

        assertEquals("Test#method", stats.methodId)
        assertEquals("Test", stats.className)
        assertEquals("method", stats.methodName)
        assertEquals(100, stats.callCount)
        assertEquals(25.5, stats.avgDurationMs, 0.01)
        assertEquals(500L, stats.maxDurationMs)
        assertEquals(5L, stats.minDurationMs)
        assertEquals(3.2, stats.avgDepth, 0.01)
        assertEquals(10, stats.maxDepth)
    }
}

private class TestTraceMetricsRepository : TraceMetricsRepository {
    val metrics = mutableMapOf<String, TraceMetricEntity>()
    val rawMetrics = mutableListOf<TraceMetricRawEntity>()
    private val metricsFlow = MutableStateFlow<List<TraceMetricEntity>>(emptyList())

    override fun getMetricsFlow(sortType: SortType): Flow<List<TraceMetricEntity>> = metricsFlow

    override fun getMetricsPaged(sortType: SortType): Flow<PagingData<TraceMetricEntity>> =
        metricsFlow.map { PagingData.from(it) }

    override suspend fun upsertMetric(asmMetric: AsmTraceMetric) {
        rawMetrics.add(
            TraceMetricRawEntity(
                methodId = asmMetric.id,
                className = asmMetric.className,
                methodName = asmMetric.methodName,
                durationMs = asmMetric.durationMs,
                startTimeMs = asmMetric.startTimeMs,
                threadName = asmMetric.threadName,
                executionId = asmMetric.executionId,
                parentExecutionId = asmMetric.parentExecutionId,
                parentMethodId = asmMetric.parentMethodId,
                depth = asmMetric.depth,
            )
        )

        // Update aggregated metrics
        val existing = metrics[asmMetric.id]
        val entity = existing?.copy(
            count = existing.count + 1,
            maxDurationMs = max(existing.maxDurationMs, asmMetric.durationMs),
            lastDurationMs = asmMetric.durationMs,
            lastStartTimeMs = asmMetric.startTimeMs,
            lastThreadName = asmMetric.threadName,
            updatedAt = System.currentTimeMillis()
        ) ?: TraceMetricEntity(
            id = asmMetric.id,
            className = asmMetric.className,
            methodName = asmMetric.methodName,
            count = 1,
            maxDurationMs = asmMetric.durationMs,
            lastDurationMs = asmMetric.durationMs,
            lastStartTimeMs = asmMetric.startTimeMs,
            lastThreadName = asmMetric.threadName
        )
        metrics[asmMetric.id] = entity
        metricsFlow.value = metrics.values.toList()
    }

    override suspend fun clear() {
        metrics.clear()
        rawMetrics.clear()
        metricsFlow.value = emptyList()
    }

    override suspend fun getCallStackForExecution(executionId: Long): List<TraceMetricRawEntity> {
        val result = mutableListOf<TraceMetricRawEntity>()
        var current = rawMetrics.find { it.executionId == executionId }
        while (current != null) {
            result.add(current)
            current = current.parentExecutionId?.let { parentId ->
                rawMetrics.find { it.executionId == parentId }
            }
        }
        return result.sortedBy { it.depth }
    }

    override suspend fun getCallTree(rootExecutionId: Long): List<TraceMetricRawEntity> {
        val result = mutableListOf<TraceMetricRawEntity>()
        fun collectTree(execId: Long) {
            val node = rawMetrics.find { it.executionId == execId } ?: return
            result.add(node)
            rawMetrics.filter { it.parentExecutionId == execId }.forEach {
                collectTree(it.executionId)
            }
        }
        collectTree(rootExecutionId)
        return result.sortedWith(compareBy({ it.depth }, { it.startTimeMs }))
    }

    override suspend fun getChildCalls(executionId: Long): List<TraceMetricRawEntity> {
        return rawMetrics.filter { it.parentExecutionId == executionId }
            .sortedBy { it.startTimeMs }
    }

    override suspend fun getSlowRootCalls(thresholdMs: Long): List<TraceMetricRawEntity> {
        return rawMetrics.filter { it.depth == 0 && it.durationMs > thresholdMs }
            .sortedByDescending { it.durationMs }
    }

    override suspend fun getCallsInTimeRange(startMs: Long, endMs: Long): List<TraceMetricRawEntity> {
        return rawMetrics.filter { it.startTimeMs in startMs..endMs }
            .sortedWith(compareBy({ it.threadName }, { it.startTimeMs }, { it.depth }))
    }

    override suspend fun getMethodStats(): List<MethodStatsEntity> {
        return rawMetrics.groupBy { it.methodId }.map { (methodId, traces) ->
            MethodStatsEntity(
                methodId = methodId,
                className = traces.first().className,
                methodName = traces.first().methodName,
                callCount = traces.size,
                avgDurationMs = traces.map { it.durationMs }.average(),
                maxDurationMs = traces.maxOf { it.durationMs },
                minDurationMs = traces.minOf { it.durationMs },
                avgDepth = traces.map { it.depth }.average(),
                maxDepth = traces.maxOf { it.depth },
            )
        }
    }

    override suspend fun getAllRawMetrics(): List<TraceMetricRawEntity> {
        return rawMetrics.sortedWith(compareBy({ it.threadName }, { it.startTimeMs }, { it.depth }))
    }
}
