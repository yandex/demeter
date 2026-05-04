package com.yandex.demeter.plugin

import com.yandex.demeter.coroutine.tracer.plugin.asm.CoroutineTracerClassVisitor
import com.yandex.test.coroutine.TestCoroutineAsync
import com.yandex.test.coroutine.TestCoroutineLaunch
import com.yandex.test.coroutine.TestCoroutineMultipleLaunches
import com.yandex.test.coroutine.TestCoroutineNestedLaunch
import com.yandex.test.coroutine.TestNoCoroutines
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DemeterCoroutineTracerPluginTest {

    @Test
    fun `check applied plugin`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply {
            apply("com.android.application")
            apply("com.yandex.demeter.coroutine.tracer")
        }

        assertTrue(project.pluginManager.hasPlugin("com.yandex.demeter.coroutine.tracer"))
        assertEquals(
            "com.yandex.demeter.coroutine.tracer",
            project.pluginManager.findPlugin("com.yandex.demeter.coroutine.tracer")?.id
        )
    }

    @Test
    fun `test launch instrumentation`() {
        val visitor = instrumentClass<TestCoroutineLaunch>()

        assertTrue(
            (visitor.methodsCalled["onCoroutineLaunched"] ?: 0) > 0,
            "Expected onCoroutineLaunched to be injected for launch call"
        )
    }

    @Test
    fun `test async instrumentation`() {
        val visitor = instrumentClass<TestCoroutineAsync>()

        assertTrue(
            (visitor.methodsCalled["onCoroutineLaunched"] ?: 0) > 0,
            "Expected onCoroutineLaunched to be injected for async call"
        )
    }

    @Test
    fun `test class without coroutines`() {
        val visitor = instrumentClass<TestNoCoroutines>()

        assertNull(
            visitor.methodsCalled["onCoroutineLaunched"],
            "Expected no onCoroutineLaunched calls for a class without coroutines"
        )
    }

    @Test
    fun `test nested launch injects for outer launch in host class`() {
        val visitor = instrumentClass<TestCoroutineNestedLaunch>()

        // Inner launch is compiled into a separate continuation class,
        // so only the outer launch is instrumented in the host class.
        assertEquals(
            1,
            visitor.methodsCalled["onCoroutineLaunched"],
            "Expected onCoroutineLaunched for outer launch in host class"
        )
    }

    @Test
    fun `test multiple launches and async injects for each builder call`() {
        val visitor = instrumentClass<TestCoroutineMultipleLaunches>()

        assertEquals(
            3,
            visitor.methodsCalled["onCoroutineLaunched"],
            "Expected onCoroutineLaunched for 2 launches + 1 async"
        )
    }

    @Test
    fun `test launch site string is injected as LDC constant`() {
        val visitor = instrumentClass<TestCoroutineLaunch>()

        assertTrue(
            visitor.ldcConstants.any { it is String && it.contains("TestCoroutineLaunch") && it.contains("#") },
            "Expected launch site string containing class name and '#' separator, got: ${visitor.ldcConstants}"
        )
    }

    @Test
    fun `test DUP instruction precedes onCoroutineLaunched call`() {
        val visitor = instrumentClass<TestCoroutineLaunch>()

        assertTrue(
            visitor.dupBeforeOnCoroutineLaunched,
            "Expected DUP instruction before onCoroutineLaunched to preserve Job on stack"
        )
    }

    private inline fun <reified T> instrumentClass(): InstrumentationTrackingVisitor {
        val className = T::class.qualifiedName.orEmpty()
        val reader = ClassReader(className)
        val visitor = InstrumentationTrackingVisitor()
        val coroutineTracerClassVisitor = CoroutineTracerClassVisitor(visitor, className)
        reader.accept(coroutineTracerClassVisitor, 0)
        return visitor
    }
}

private class InstrumentationTrackingVisitor : ClassVisitor(ASM_API_VERSION) {
    val methodsCalled = hashMapOf<String, Int>()
    val ldcConstants = mutableListOf<Any>()
    var dupBeforeOnCoroutineLaunched = false

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String?,
        signature: String?,
        exceptions: Array<String?>?,
    ): MethodVisitor {
        return TrackingMethodVisitor(this)
    }

    fun addMethodCalled(name: String) {
        methodsCalled[name] = methodsCalled.getOrPut(name) { 0 } + 1
    }
}

private class TrackingMethodVisitor(
    private val parent: InstrumentationTrackingVisitor,
) : MethodVisitor(ASM_API_VERSION) {
    // Tracks instruction sequence: DUP -> LDC -> INVOKESTATIC(onCoroutineLaunched)
    private var sawDup = false
    private var sawDupThenLdc = false

    override fun visitInsn(opcode: Int) {
        if (opcode == org.objectweb.asm.Opcodes.DUP) {
            sawDup = true
        } else {
            sawDup = false
            sawDupThenLdc = false
        }
        super.visitInsn(opcode)
    }

    override fun visitLdcInsn(value: Any) {
        parent.ldcConstants.add(value)
        if (sawDup) {
            sawDupThenLdc = true
        }
        sawDup = false
        super.visitLdcInsn(value)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String,
        isInterface: Boolean,
    ) {
        parent.addMethodCalled(name)
        if (name == "onCoroutineLaunched" && sawDupThenLdc) {
            parent.dupBeforeOnCoroutineLaunched = true
        }
        sawDup = false
        sawDupThenLdc = false
        super.visitMethodInsn(opcode, owner, name, desc, isInterface)
    }
}
