package com.yandex.demeter.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yandex.demeter.profiler.inject.internal.asm.AbstractInjectAsm
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@OrderWith(Alphanumeric::class)
class InjectAsmBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var className: String
    private lateinit var clazz: Class<*>

    private lateinit var random: Random

    @Before
    fun setup() {
        className = this::class.qualifiedName.orEmpty()
        clazz = this::class.java
        random = Random(12_04_2019)
    }

    @Test
    fun log() {
        var startTime = 0L
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                startTime = random.nextLong()
            }
            StubInjectAsm.log(
                startTimeNs = startTime,
                className = className,
                initializedClass = clazz,
            )
        }
    }

    object StubInjectAsm : AbstractInjectAsm(
        queue = Channel(onBufferOverflow = BufferOverflow.DROP_OLDEST),
    ) {
        fun log(
            startTimeNs: Long,
            className: String,
            initializedClass: Class<*>,
        ) {
            logInternal(
                startTimeNs = startTimeNs,
                className = className,
                initializedClass = initializedClass,
            )
        }
    }
}
