package com.yandex.demeter.profiler.tracer

import com.yandex.demeter.profiler.tracer.internal.asm.TracerAsm
import com.yandex.demeter.profiler.tracer.internal.data.AsmTraceMetricsHandler
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

private const val TEST_CLASS_NAME = "TEST_CLASS_NAME"
private const val TEST_METHOD_NAME = "TEST_METHOD_NAME"
private const val TEST_FULL_METHOD_NAME = "$TEST_CLASS_NAME#$TEST_METHOD_NAME"

class DemeterTracerTest {

    private val testScope = TestScope()

    @BeforeTest
    fun setup() {
        AsmTraceMetricsHandler.init(testScope)
    }

    @Test
    fun `calculate max time for one event`() {
        TracerAsm.beginSection(startTimeNs = -100, fullMethodName = TEST_FULL_METHOD_NAME)

        // relative to 0 as current time
        TracerAsm.endSection(
            startTimeNs = -100,
            fullMethodName = TEST_FULL_METHOD_NAME,
            className = TEST_CLASS_NAME,
            methodName = TEST_METHOD_NAME,
        )

        TracerAsm.beginSection(startTimeNs = -300, fullMethodName = TEST_FULL_METHOD_NAME)
        TracerAsm.endSection(
            startTimeNs = -300,
            fullMethodName = TEST_FULL_METHOD_NAME,
            className = TEST_CLASS_NAME,
            methodName = TEST_METHOD_NAME,
        )

        testScope.launch {
            assertEquals(1, TraceMetricsRepository.metrics.size)

            val metric = TraceMetricsRepository.metrics.values.first()
            assertEquals(2, metric.count)
            assertEquals(300, metric.maxTime)
        }
    }

    @Test
    fun `collect two different events`() {
        TracerAsm.beginSection(startTimeNs = -100, fullMethodName = TEST_FULL_METHOD_NAME)
        TracerAsm.endSection(
            startTimeNs = -100,
            fullMethodName = TEST_FULL_METHOD_NAME,
            className = TEST_CLASS_NAME,
            methodName = TEST_METHOD_NAME,
        )

        InnerCollector.innerMetricCollector()

        testScope.launch {
            assertEquals(2, TraceMetricsRepository.metrics.size)

            val metric100 = TraceMetricsRepository.metrics.values.first { it.maxTime == 100L }
            val metric500 = TraceMetricsRepository.metrics.values.first { it.maxTime == 500L }

            assertEquals(1, metric100.count)
            assertEquals(100, metric100.maxTime)
            assertEquals(1, metric500.count)
            assertEquals(500, metric500.maxTime)
        }
    }
}

object InnerCollector {
    fun innerMetricCollector() {
        TracerAsm.beginSection(startTimeNs = -500, fullMethodName = TEST_FULL_METHOD_NAME)
        TracerAsm.endSection(
            startTimeNs = -500,
            fullMethodName = TEST_FULL_METHOD_NAME,
            className = TEST_CLASS_NAME,
            methodName = TEST_METHOD_NAME,
        )
    }
}
