package com.yandex.demeter.plugin

import com.yandex.demeter.tracer.plugin.asm.TracerClassVisitor
import com.yandex.test.TestApply
import com.yandex.test.TestApplyWithParameters
import com.yandex.test.TestClass
import com.yandex.test.TestClassWithSomeExitsInMethod
import com.yandex.test.TestClassWithSomeThrows
import com.yandex.test.TestObject
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import kotlin.test.assertEquals

class DemeterTracerPluginTest {
    @Test
    fun `check applied plugin`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply {
            apply("com.android.application")
            apply("com.yandex.demeter.tracer")
        }

        assertTrue(project.pluginManager.hasPlugin("com.yandex.demeter.tracer"))
        assertEquals(
            "com.yandex.demeter.tracer",
            project.pluginManager.findPlugin("com.yandex.demeter.tracer")?.id
        )
    }

    @Test
    fun `test object`() {
        val reader = ClassReader(TestObject::class.qualifiedName)
        val methodsClassVisitor = MethodsCalledClassVisitor()
        val tracerClassVisitor = TracerClassVisitor(
            methodsClassVisitor,
            TestObject::class.qualifiedName.orEmpty(),
        )
        reader.accept(tracerClassVisitor, 0)

        assertEquals(3, methodsClassVisitor.methodsCalled["nanoTime"])
        assertEquals(1, methodsClassVisitor.methodsCalled["println"])

        assertEquals(3, methodsClassVisitor.methodsCalled["beginSection"])
        assertEquals(3, methodsClassVisitor.methodsCalled["endSection"])
    }

    @Test
    fun `test class`() {
        val reader = ClassReader(TestClass::class.qualifiedName)
        val methodsClassVisitor = MethodsCalledClassVisitor()
        val tracerClassVisitor = TracerClassVisitor(
            methodsClassVisitor,
            TestClass::class.qualifiedName.orEmpty(),
        )
        reader.accept(tracerClassVisitor, 0)

        assertEquals(2, methodsClassVisitor.methodsCalled["nanoTime"])
        assertEquals(3, methodsClassVisitor.methodsCalled["println"])

        assertEquals(2, methodsClassVisitor.methodsCalled["beginSection"])
        assertEquals(2, methodsClassVisitor.methodsCalled["endSection"])
    }

    @Test
    fun `test class with apply in constructor`() {
        val reader = ClassReader(TestApply::class.qualifiedName)
        val methodsClassVisitor = MethodsCalledClassVisitor()
        val tracerClassVisitor = TracerClassVisitor(
            methodsClassVisitor,
            TestApply::class.qualifiedName.orEmpty(),
        )
        reader.accept(tracerClassVisitor, 0)

        assertEquals(2, methodsClassVisitor.methodsCalled["nanoTime"])
        assertEquals(3, methodsClassVisitor.methodsCalled["println"])

        assertEquals(2, methodsClassVisitor.methodsCalled["beginSection"])
        assertEquals(2, methodsClassVisitor.methodsCalled["endSection"])
    }

    @Test
    fun `test class with apply in constructor with parameters`() {
        val reader = ClassReader(TestApplyWithParameters::class.qualifiedName)
        val methodsClassVisitor = MethodsCalledClassVisitor()
        val tracerClassVisitor = TracerClassVisitor(
            methodsClassVisitor,
            TestApplyWithParameters::class.qualifiedName.orEmpty(),
        )
        reader.accept(tracerClassVisitor, 0)

        assertEquals(2, methodsClassVisitor.methodsCalled["nanoTime"])
        assertEquals(3, methodsClassVisitor.methodsCalled["println"])

        assertEquals(2, methodsClassVisitor.methodsCalled["beginSection"])
        assertEquals(2, methodsClassVisitor.methodsCalled["endSection"])
    }

    @Test
    fun `test class with some throws in constructor`() {
        val reader = ClassReader(TestClassWithSomeThrows::class.qualifiedName)
        val methodsClassVisitor = MethodsCalledClassVisitor()
        val tracerClassVisitor = TracerClassVisitor(
            methodsClassVisitor,
            TestClassWithSomeThrows::class.qualifiedName.orEmpty(),
        )
        reader.accept(tracerClassVisitor, ClassReader.SKIP_FRAMES)

        assertEquals(1, methodsClassVisitor.methodsCalled["nanoTime"])

        assertEquals(1, methodsClassVisitor.methodsCalled["beginSection"])
        assertEquals(5, methodsClassVisitor.methodsCalled["endSection"])
    }

    @Test
    fun `test class with some exits in method`() {
        val reader = ClassReader(TestClassWithSomeExitsInMethod::class.qualifiedName)
        val methodsClassVisitor = MethodsCalledClassVisitor()
        val tracerClassVisitor = TracerClassVisitor(
            methodsClassVisitor,
            TestClassWithSomeExitsInMethod::class.qualifiedName.orEmpty(),
        )
        reader.accept(tracerClassVisitor, ClassReader.SKIP_FRAMES)

        assertEquals(2, methodsClassVisitor.methodsCalled["nanoTime"])

        assertEquals(2, methodsClassVisitor.methodsCalled["beginSection"])
        assertEquals(12, methodsClassVisitor.methodsCalled["endSection"])
    }
}

private class MethodsCalledClassVisitor : ClassVisitor(ASM_API_VERSION) {
    val methodsCalled = hashMapOf<String, Int>()

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String?,
        signature: String?,
        exceptions: Array<String?>?,
    ): MethodVisitor {
        return SavingMethodVisitor(this)
    }

    fun addMethodCalled(name: String) {
        methodsCalled[name] = methodsCalled.getOrPut(name) { 0 } + 1
    }
}

private class SavingMethodVisitor(
    private val visit: MethodsCalledClassVisitor,
) : MethodVisitor(ASM_API_VERSION) {
    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String,
        isInterface: Boolean,
    ) {
        visit.addMethodCalled(name)
        super.visitMethodInsn(opcode, owner, name, desc, isInterface)
    }
}
