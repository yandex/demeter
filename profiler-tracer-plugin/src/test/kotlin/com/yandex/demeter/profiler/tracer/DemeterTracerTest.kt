package com.yandex.demeter.profiler.tracer

import androidx.paging.PagingData
import com.yandex.demeter.internal.utils.SortType
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsRepository
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricEntity
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

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

    private fun createAsmMetric(
        id: String = TEST_ID,
        className: String = TEST_CLASS_NAME,
        methodName: String = TEST_METHOD_NAME,
        startTimeMs: Long = System.currentTimeMillis(),
        durationMs: Long = 100
    ): AsmTraceMetric {
        val startTimeNs = startTimeMs * 1_000_000
        val finishTimeNs = startTimeNs + (durationMs * 1_000_000)
        return AsmTraceMetric(
            id = id,
            className = className,
            methodName = methodName,
            startTimeNs = startTimeNs,
            finishTimeNs = finishTimeNs,
            threadName = "test-thread"
        )
    }
}

private class TestTraceMetricsRepository : TraceMetricsRepository {
    val metrics = mutableMapOf<String, TraceMetricEntity>()
    private val metricsFlow = MutableStateFlow<List<TraceMetricEntity>>(emptyList())

    override fun getMetricsFlow(sortType: SortType): Flow<List<TraceMetricEntity>> = metricsFlow

    override fun getMetricsPaged(sortType: SortType): Flow<PagingData<TraceMetricEntity>> =
        metricsFlow.map { PagingData.from(it) }

    override suspend fun upsertMetric(asmMetric: AsmTraceMetric) {
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
        metricsFlow.value = emptyList()
    }
}
