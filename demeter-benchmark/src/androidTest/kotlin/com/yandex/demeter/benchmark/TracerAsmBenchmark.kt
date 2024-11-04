package com.yandex.demeter.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import com.yandex.demeter.profiler.tracer.internal.asm.AbstractTracerAsm
import com.yandex.demeter.profiler.tracer.internal.asm.SystemTraceBuffer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric
import org.junit.runners.Parameterized
import kotlin.random.Random

@RunWith(Parameterized::class)
@OrderWith(Alphanumeric::class)
class TracerAsmBenchmark(
    private val profilerEnabled: Boolean,
) {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var asm: StubTracerAsm

    private lateinit var fullMethodName: String
    private lateinit var className: String
    private lateinit var methodName: String

    private lateinit var random: Random

    @Before
    fun setup() {
        className = this::class.qualifiedName.orEmpty()
        methodName = "setup"
        fullMethodName = "$className#$methodName"
        random = Random(12_04_2019)
        asm = StubTracerAsm(isProfilerEnabled = profilerEnabled)
    }

    @Test
    fun beginSectionAndEndSection() {
        var startTime = 0L
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                startTime = random.nextLong()
            }
            asm.beginSection(
                startTimeNs = startTime,
                fullMethodName = fullMethodName,
            )
            asm.endSection(
                startTimeNs = startTime,
                fullMethodName = fullMethodName,
                className = className,
                methodName = methodName,
            )
        }
    }

    @Test
    fun beginSection() {
        var startTime = 0L
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                startTime = random.nextLong()
            }
            asm.beginSection(
                startTimeNs = startTime,
                fullMethodName = fullMethodName,
            )
        }
    }

    @Test
    fun endSection() {
        var startTime = 0L
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                startTime = random.nextLong()
            }
            asm.endSection(
                startTimeNs = startTime,
                fullMethodName = fullMethodName,
                className = className,
                methodName = methodName,
            )
        }
    }

    class StubTracerAsm(
        isProfilerEnabled: Boolean,
    ) : AbstractTracerAsm(
        queue = Channel(onBufferOverflow = BufferOverflow.DROP_OLDEST),
        systemTraceBufferBeginSection = if (isProfilerEnabled) SystemTraceBuffer::beginSection else { _, _ -> Unit },
        systemTraceBufferEndSection = if (isProfilerEnabled) SystemTraceBuffer::endSection else { _, _ -> Unit },
    ) {

        fun beginSection(
            startTimeNs: Long,
            fullMethodName: String,
        ) {
            beginSectionInternal(
                startTimeNs = startTimeNs,
                fullMethodName = fullMethodName,
            )
        }

        fun endSection(
            startTimeNs: Long,
            fullMethodName: String,
            className: String,
            methodName: String,
        ) {
            endSectionInternal(
                startTimeNs = startTimeNs,
                fullMethodName = fullMethodName,
                className = className,
                methodName = methodName,
            )
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "profilerEnabled={0}")
        fun data(): Iterable<Array<Any>> = listOf(
            arrayOf(true),
            arrayOf(false),
        )
    }
}
