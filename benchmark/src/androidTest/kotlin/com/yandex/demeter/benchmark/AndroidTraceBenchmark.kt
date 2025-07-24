package com.yandex.demeter.benchmark

import android.os.Build
import android.os.Trace
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@OrderWith(Alphanumeric::class)
class AndroidTraceBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun traceIsEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        benchmarkRule.measureRepeated {
            Trace.isEnabled()
        }
    }

    @Test
    fun beginAsyncSection() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        if (!Trace.isEnabled()) {
            return
        }
        val methodName = "beginAsyncSection"
        var cookie = 0
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                cookie = Random.nextInt()
            }
            Trace.beginAsyncSection(methodName, cookie)
        }
    }

    @Test
    fun endAsyncSection() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        if (!Trace.isEnabled()) {
            return
        }
        val methodName = "endAsyncSection"
        var cookie = 0
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                cookie = Random.nextInt()
            }
            Trace.endAsyncSection(methodName, cookie)
        }
    }

    @Test
    fun beginAsyncAndEndAsyncSection() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        if (!Trace.isEnabled()) {
            return
        }
        val methodName = "beginAsyncAndEndAsyncSection"
        var cookie = 0
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                cookie = Random.nextInt()
            }
            Trace.beginAsyncSection(methodName, cookie)
            Trace.endAsyncSection(methodName, cookie)
        }
    }

    @Test
    fun beginSection() {
        if (!Trace.isEnabled()) {
            return
        }
        val methodName = "beginSection"
        benchmarkRule.measureRepeated {
            Trace.beginSection(methodName)
        }
    }

    @Test
    fun endSection() {
        if (!Trace.isEnabled()) {
            return
        }
        benchmarkRule.measureRepeated {
            Trace.endSection()
        }
    }

    @Test
    fun beginAndEndSection() {
        if (!Trace.isEnabled()) {
            return
        }
        val methodName = "beginAndEndSection"
        benchmarkRule.measureRepeated {
            Trace.beginSection(methodName)
            Trace.endSection()
        }
    }
}