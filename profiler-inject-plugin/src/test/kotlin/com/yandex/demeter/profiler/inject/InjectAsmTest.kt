package com.yandex.demeter.profiler.inject

import com.yandex.demeter.profiler.inject.internal.asm.InjectAsm
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InjectAsmTest {
    @Test
    fun `new metadata and legacy calls are consumed without loading classes`() = runTest {
        val events = async { InjectAsm.metricsQueue.take(2).toList() }
        InjectAsm.log(System.nanoTime(), "missing.Owner", "missing.Dependency;long")
        InjectAsm.log(System.nanoTime(), "missing.Legacy", Any::class.java)
        val metrics = events.await()
        assertEquals(listOf("missing.Owner", "missing.Legacy"), metrics.map { it.className })
        assertEquals(listOf("missing.Dependency;long", ""), metrics.map { it.parameterClassNames })
        assertTrue(metrics.all { it.durationMs >= 0 })
    }
}