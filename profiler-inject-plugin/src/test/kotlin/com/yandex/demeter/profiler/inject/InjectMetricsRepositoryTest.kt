package com.yandex.demeter.profiler.inject

import com.yandex.demeter.profiler.inject.internal.data.InjectMetricsRepository
import com.yandex.demeter.profiler.inject.internal.data.model.AsmInjectMetric
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InjectMetricsRepositoryTest {
    @BeforeEach
    fun clear() = InjectMetricsRepository.clear()

    @Test
    fun `original name need not be a loadable class`() {
        record("com.yandex.demeter.PreferencesProvider")
        val metric = InjectMetricsRepository.initializedMetrics.values.single()
        assertEquals("PreferencesProvider", metric.simpleName)
        assertEquals(3, metric.initTime)
        assertEquals("test-thread", metric.threadName)
    }

    @Test
    fun `declared constructor parameters form a dependency tree`() {
        record("missing.Leaf")
        record("missing.Dependency", "missing.Leaf")
        record("missing.Owner", "missing.Dependency;int;java.lang.String[]")
        val root = InjectMetricsRepository.initializedMetrics.values.single()
        assertEquals("Owner", root.simpleName)
        assertEquals("Dependency", root.args.single().simpleName)
        assertEquals("Leaf", root.args.single().args.single().simpleName)
        assertEquals(3, root.initTime)
    }

    @Test
    fun `consuming a dependency does not reset instance numbering`() {
        record("missing.Dependency")
        record("missing.Owner", "missing.Dependency")
        record("missing.Dependency")
        record("missing.Owner", "missing.Dependency")
        val roots = InjectMetricsRepository.initializedMetrics.values.toList()
        assertEquals(listOf("Owner", "Owner №1"), roots.map { it.simpleName })
        assertEquals("Dependency №1", roots.last().args.single().simpleName)
    }

    @Test
    fun `overloaded constructors use their own parameter metadata`() {
        record("missing.First")
        record("missing.Owner", "missing.First")
        record("missing.Second")
        record("missing.Owner", "missing.Second")
        val roots = InjectMetricsRepository.initializedMetrics.values.toList()
        assertEquals("First", roots[0].args.single().simpleName)
        assertEquals("Second", roots[1].args.single().simpleName)
    }

    @Test
    fun `base and derived constructors have independent identities`() {
        record("missing.Base")
        record("missing.Derived")
        assertEquals(listOf("Base", "Derived"), InjectMetricsRepository.initializedMetrics.values.map { it.simpleName })
    }

    @Test
    fun `interfaces providers and lazy parameters do not guess concrete instances`() {
        record("missing.Implementation")
        record("missing.Owner", "missing.Interface;javax.inject.Provider;dagger.Lazy")
        assertEquals(2, InjectMetricsRepository.initializedMetrics.size)
        assertTrue(InjectMetricsRepository.initializedMetrics.getValue("missing.Owner").args.isEmpty())
    }

    @Test
    fun `multiple candidate instances are left unmatched`() {
        record("missing.Dependency")
        record("missing.Dependency")
        record("missing.Owner", "missing.Dependency")
        assertEquals(3, InjectMetricsRepository.initializedMetrics.size)
        assertTrue(InjectMetricsRepository.initializedMetrics.getValue("missing.Owner").args.isEmpty())
    }

    @Test
    fun `two parameters with the same type are left unmatched`() {
        record("missing.Dependency")
        record("missing.Owner", "missing.Dependency;missing.Dependency")
        assertEquals(2, InjectMetricsRepository.initializedMetrics.size)
        assertTrue(InjectMetricsRepository.initializedMetrics.getValue("missing.Owner").args.isEmpty())
    }

    @Test
    fun `cached metadata links unique parameters and skips repeated types`() {
        record("missing.Repeated")
        repeat(2) {
            record("missing.Unique")
            record("missing.Owner", "missing.Repeated;missing.Unique;missing.Repeated")
        }
        val metrics = InjectMetricsRepository.initializedMetrics
        assertEquals(listOf("missing.Repeated", "missing.Owner", "missing.Owner №1"), metrics.keys.toList())
        assertEquals("Unique", metrics.getValue("missing.Owner").args.single().simpleName)
        assertEquals("Unique №1", metrics.getValue("missing.Owner №1").args.single().simpleName)
    }

    @Test
    fun `clear resets counters and dependency candidates`() {
        record("missing.Dependency")
        record("missing.Dependency")
        InjectMetricsRepository.clear()
        record("missing.Dependency")
        record("missing.Owner", "missing.Dependency")
        val metric = InjectMetricsRepository.initializedMetrics.values.single()
        assertEquals("Dependency", metric.args.single().simpleName)
    }

    private fun record(name: String, parameters: String = "") {
        InjectMetricsRepository.putMetric(
            AsmInjectMetric(name, parameters, 1_000_000, 4_000_000, "test-thread")
        )
    }
}