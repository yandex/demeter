package com.yandex.demeter.profiler.coroutine.tracer

import com.yandex.demeter.profiler.coroutine.tracer.internal.data.CoroutineMetricsRepository
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.CoroutineMetricRawEntity
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.AsmCoroutineMetric
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.CoroutineTraceNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val NANOSECONDS_IN_MILLISECOND = 1_000_000L

class CoroutineTracerTest {

    private lateinit var testRepository: TestCoroutineMetricsRepository

    @BeforeTest
    fun setup() {
        testRepository = TestCoroutineMetricsRepository()
    }

    @Test
    fun `upsert single coroutine metric`() = runTest {
        val metric = createCoroutineMetric(
            traceId = 1L,
            launchSite = "TestClass#testMethod",
            durationMs = 100,
        )

        testRepository.upsertMetric(metric)

        val rawMetrics = testRepository.getRawMetrics()
        assertEquals(1, rawMetrics.size)
        assertEquals("TestClass#testMethod", rawMetrics[0].launchSite)
        assertEquals(100L, rawMetrics[0].durationMs)
    }

    @Test
    fun `parent-child tree building`() = runTest {
        val root = createCoroutineMetric(
            traceId = 1L,
            parentTraceId = null,
            launchSite = "Root#launch",
            durationMs = 300,
            depth = 0,
        )
        val child = createCoroutineMetric(
            traceId = 2L,
            parentTraceId = 1L,
            launchSite = "Child#launch",
            durationMs = 200,
            depth = 1,
        )
        val grandchild = createCoroutineMetric(
            traceId = 3L,
            parentTraceId = 2L,
            launchSite = "Grandchild#launch",
            durationMs = 100,
            depth = 2,
        )

        testRepository.upsertMetric(root)
        testRepository.upsertMetric(child)
        testRepository.upsertMetric(grandchild)

        val tree = testRepository.getCoroutineTree(1L)
        assertNotNull(tree)
        assertEquals(1L, tree.traceId)
        assertEquals("Root#launch", tree.launchSite)
        assertEquals(1, tree.children.size)

        val childNode = tree.children[0]
        assertEquals(2L, childNode.traceId)
        assertEquals("Child#launch", childNode.launchSite)
        assertEquals(1, childNode.children.size)

        val grandchildNode = childNode.children[0]
        assertEquals(3L, grandchildNode.traceId)
        assertEquals("Grandchild#launch", grandchildNode.launchSite)
        assertEquals(0, grandchildNode.children.size)
    }

    @Test
    fun `cancelled coroutine tracking`() = runTest {
        val metric = createCoroutineMetric(
            traceId = 1L,
            launchSite = "Cancelled#launch",
            isCancelled = true,
            durationMs = 50,
        )

        testRepository.upsertMetric(metric)

        val cancelled = testRepository.getCancelledCoroutines()
        assertEquals(1, cancelled.size)
        assertTrue(cancelled[0].isCancelled)
        assertEquals("Cancelled#launch", cancelled[0].launchSite)
    }

    @Test
    fun `exception tracking`() = runTest {
        val metric = createCoroutineMetric(
            traceId = 1L,
            launchSite = "Failing#launch",
            exception = "IllegalStateException: something went wrong",
            durationMs = 75,
        )

        testRepository.upsertMetric(metric)

        val rawMetrics = testRepository.getRawMetrics()
        assertEquals(1, rawMetrics.size)
        assertEquals("IllegalStateException: something went wrong", rawMetrics[0].exception)
    }

    @Test
    fun `slow coroutines filter`() = runTest {
        val fast = createCoroutineMetric(
            traceId = 1L,
            launchSite = "Fast#launch",
            durationMs = 50,
        )
        val slow = createCoroutineMetric(
            traceId = 2L,
            launchSite = "Slow#launch",
            durationMs = 500,
        )

        testRepository.upsertMetric(fast)
        testRepository.upsertMetric(slow)

        val slowCoroutines = testRepository.getSlowCoroutines(thresholdMs = 100)
        assertEquals(1, slowCoroutines.size)
        assertEquals("Slow#launch", slowCoroutines[0].launchSite)
    }

    @Test
    fun `get cancelled coroutines`() = runTest {
        val normal = createCoroutineMetric(
            traceId = 1L,
            launchSite = "Normal#launch",
            isCancelled = false,
        )
        val cancelled1 = createCoroutineMetric(
            traceId = 2L,
            launchSite = "Cancelled1#launch",
            isCancelled = true,
        )
        val cancelled2 = createCoroutineMetric(
            traceId = 3L,
            launchSite = "Cancelled2#launch",
            isCancelled = true,
        )

        testRepository.upsertMetric(normal)
        testRepository.upsertMetric(cancelled1)
        testRepository.upsertMetric(cancelled2)

        val cancelled = testRepository.getCancelledCoroutines()
        assertEquals(2, cancelled.size)
        assertTrue(cancelled.all { it.isCancelled })
    }

    @Test
    fun `tree roots returns only depth-0 nodes`() = runTest {
        val root1 = createCoroutineMetric(
            traceId = 1L,
            parentTraceId = null,
            launchSite = "Root1#launch",
            depth = 0,
        )
        val child1 = createCoroutineMetric(
            traceId = 2L,
            parentTraceId = 1L,
            launchSite = "Child1#launch",
            depth = 1,
        )
        val root2 = createCoroutineMetric(
            traceId = 3L,
            parentTraceId = null,
            launchSite = "Root2#launch",
            depth = 0,
        )

        testRepository.upsertMetric(root1)
        testRepository.upsertMetric(child1)
        testRepository.upsertMetric(root2)

        val roots = testRepository.rawMetrics.filter { it.depth == 0 }
        assertEquals(2, roots.size)
        assertTrue(roots.all { it.depth == 0 })
        assertTrue(roots.all { it.parentTraceId == null })
    }

    @Test
    fun `multiple root trees`() = runTest {
        // Tree 1: root1 -> child1
        val root1 = createCoroutineMetric(
            traceId = 1L,
            parentTraceId = null,
            launchSite = "Root1#launch",
            durationMs = 200,
            depth = 0,
        )
        val child1 = createCoroutineMetric(
            traceId = 2L,
            parentTraceId = 1L,
            launchSite = "Child1#launch",
            durationMs = 100,
            depth = 1,
        )

        // Tree 2: root2 -> child2
        val root2 = createCoroutineMetric(
            traceId = 3L,
            parentTraceId = null,
            launchSite = "Root2#launch",
            durationMs = 400,
            depth = 0,
        )
        val child2 = createCoroutineMetric(
            traceId = 4L,
            parentTraceId = 3L,
            launchSite = "Child2#launch",
            durationMs = 300,
            depth = 1,
        )

        testRepository.upsertMetric(root1)
        testRepository.upsertMetric(child1)
        testRepository.upsertMetric(root2)
        testRepository.upsertMetric(child2)

        // Verify tree 1
        val tree1 = testRepository.getCoroutineTree(1L)
        assertNotNull(tree1)
        assertEquals(1L, tree1.traceId)
        assertEquals(1, tree1.children.size)
        assertEquals(2L, tree1.children[0].traceId)

        // Verify tree 2
        val tree2 = testRepository.getCoroutineTree(3L)
        assertNotNull(tree2)
        assertEquals(3L, tree2.traceId)
        assertEquals(1, tree2.children.size)
        assertEquals(4L, tree2.children[0].traceId)

        // Trees are independent
        val tree1Child = tree1.children[0]
        assertTrue(tree1Child.children.isEmpty())
        assertNull(testRepository.getCoroutineTree(999L))
    }

    companion object {
        private val traceIdCounter = AtomicLong(0)
    }

    private fun createCoroutineMetric(
        traceId: Long = traceIdCounter.incrementAndGet(),
        parentTraceId: Long? = null,
        launchSite: String = "TestClass#testMethod",
        durationMs: Long = 100,
        startTimeMs: Long = System.currentTimeMillis(),
        launchThreadName: String = "test-thread",
        completionThreadName: String = "test-thread",
        isCancelled: Boolean = false,
        exception: String? = null,
        depth: Int = 0,
        dispatcherName: String? = null,
    ): AsmCoroutineMetric {
        val startTimeNs = startTimeMs * NANOSECONDS_IN_MILLISECOND
        val endTimeNs = startTimeNs + (durationMs * NANOSECONDS_IN_MILLISECOND)
        return AsmCoroutineMetric(
            traceId = traceId,
            parentTraceId = parentTraceId,
            launchSite = launchSite,
            startTimeNs = startTimeNs,
            endTimeNs = endTimeNs,
            launchThreadName = launchThreadName,
            completionThreadName = completionThreadName,
            isCancelled = isCancelled,
            exception = exception,
            depth = depth,
            dispatcherName = dispatcherName,
        )
    }
}

class TestCoroutineMetricsRepository : CoroutineMetricsRepository {
    val rawMetrics = mutableListOf<CoroutineMetricRawEntity>()
    private val rootsFlow = MutableStateFlow<List<CoroutineTraceNode>>(emptyList())

    override fun getCoroutineTreeRoots(): Flow<List<CoroutineTraceNode>> {
        return rootsFlow
    }

    override suspend fun getRawMetrics(): List<CoroutineMetricRawEntity> {
        return rawMetrics.sortedBy { it.startTimeMs }
    }

    override suspend fun getCoroutineTree(rootTraceId: Long): CoroutineTraceNode? {
        val allNodes = rawMetrics.filter {
            it.traceId == rootTraceId || isDescendantOf(it, rootTraceId)
        }
        if (allNodes.isEmpty()) return null
        return buildTree(allNodes)
    }

    override suspend fun getSlowCoroutines(thresholdMs: Long): List<CoroutineMetricRawEntity> {
        return rawMetrics.filter { it.durationMs > thresholdMs }
            .sortedByDescending { it.durationMs }
    }

    override suspend fun getCancelledCoroutines(): List<CoroutineMetricRawEntity> {
        return rawMetrics.filter { it.isCancelled }
    }

    override suspend fun clear() {
        rawMetrics.clear()
        rootsFlow.value = emptyList()
    }

    override suspend fun upsertMetric(metric: AsmCoroutineMetric) {
        rawMetrics.add(
            CoroutineMetricRawEntity(
                traceId = metric.traceId,
                parentTraceId = metric.parentTraceId,
                launchSite = metric.launchSite,
                durationMs = metric.durationMs,
                startTimeMs = metric.startTimeMs,
                launchThreadName = metric.launchThreadName,
                completionThreadName = metric.completionThreadName,
                isCancelled = metric.isCancelled,
                exception = metric.exception,
                depth = metric.depth,
            )
        )

        // Update roots flow
        val roots = rawMetrics.filter { it.depth == 0 }.map { entity ->
            toTraceNode(entity)
        }
        rootsFlow.value = roots
    }

    private fun isDescendantOf(entity: CoroutineMetricRawEntity, ancestorTraceId: Long): Boolean {
        var current = entity
        while (current.parentTraceId != null) {
            if (current.parentTraceId == ancestorTraceId) return true
            current = rawMetrics.find { it.traceId == current.parentTraceId } ?: return false
        }
        return false
    }

    private fun buildTree(entities: List<CoroutineMetricRawEntity>): CoroutineTraceNode? {
        if (entities.isEmpty()) return null

        val childrenMap = entities.groupBy { it.parentTraceId }
        val root = entities.minByOrNull { it.depth } ?: return null

        fun buildNode(entity: CoroutineMetricRawEntity): CoroutineTraceNode {
            val children = childrenMap[entity.traceId]?.map { buildNode(it) } ?: emptyList()
            return toTraceNode(entity, children)
        }

        return buildNode(root)
    }

    private fun toTraceNode(
        entity: CoroutineMetricRawEntity,
        children: List<CoroutineTraceNode> = emptyList(),
    ): CoroutineTraceNode {
        val simpleName = entity.launchSite.substringAfterLast('.').ifEmpty { entity.launchSite }
        return CoroutineTraceNode(
            traceId = entity.traceId,
            parentTraceId = entity.parentTraceId,
            launchSite = entity.launchSite,
            simpleName = simpleName,
            durationMs = entity.durationMs,
            startTimeMs = entity.startTimeMs,
            launchThreadName = entity.launchThreadName,
            completionThreadName = entity.completionThreadName,
            isCancelled = entity.isCancelled,
            exception = entity.exception,
            depth = entity.depth,
            children = children,
        )
    }
}
