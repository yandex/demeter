package com.yandex.demeter.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric

@RunWith(AndroidJUnit4::class)
@OrderWith(Alphanumeric::class)
class NopBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun nop() {
        benchmarkRule.measureRepeated {}
    }
}
