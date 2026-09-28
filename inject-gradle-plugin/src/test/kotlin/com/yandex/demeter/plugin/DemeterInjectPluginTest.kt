package com.yandex.demeter.plugin

import com.yandex.demeter.inject.plugin.asm.InjectClassVisitor
import com.yandex.demeter.profiler.inject.internal.asm.InjectAsm
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DemeterInjectPluginTest {
    @BeforeEach
    fun clear() = InjectAsm.events.clear()

    @Test
    fun `renaming classes after instrumentation preserves original constructor metadata`() {
        val original = fixture("original/Owner", "(Ljava/lang/String;J[D)V")
        val transformed = instrument(original, "original.Owner")
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassReader(transformed).accept(ClassRemapper(writer, SimpleRemapper("original/Owner", "obfuscated/a")), 0)
        val type = Loader().define(writer.toByteArray())
        type.getConstructor(String::class.java, Long::class.javaPrimitiveType, DoubleArray::class.java)
            .newInstance("value", 1L, doubleArrayOf())
        val event = InjectAsm.events.single()
        assertEquals("obfuscated.a", type.name)
        assertEquals("original.Owner", event.className)
        assertEquals("java.lang.String;long;double[]", event.parameterClassNames)
        assertTrue(event.startTimeNs > 0)
    }

    @Test
    fun `base constructor records its owner rather than the runtime subtype`() {
        val loader = Loader()
        loader.define(instrument(fixture("original/Base", "()V"), "original.Base"))
        val child = loader.define(instrument(fixture("original/Child", "()V", parent = "original/Base"), "original.Child"))
        child.getConstructor().newInstance()
        assertEquals(listOf("original.Base", "original.Child"), InjectAsm.events.map { it.className })
    }

    @Test
    fun `throwing constructors do not report a constructed instance`() {
        val type = Loader().define(instrument(fixture("original/Throwing", "()V", throws = true), "original.Throwing"))
        assertFailsWith<InvocationTargetException> { type.getConstructor().newInstance() }
        assertTrue(InjectAsm.events.isEmpty())
    }

    @Test
    fun `unannotated constructors are not instrumented`() {
        val type = Loader().define(instrument(fixture("original/Plain", "()V", injected = false), "original.Plain"))
        type.getConstructor().newInstance()
        assertTrue(InjectAsm.events.isEmpty())
    }

    @Test
    fun `each overloaded constructor emits its own descriptor metadata`() {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "original/Overloaded", null, "java/lang/Object", null)
        constructor(writer, "()V", "java/lang/Object", injected = true, throws = false)
        constructor(writer, "(I)V", "java/lang/Object", injected = true, throws = false)
        writer.visitEnd()
        val type = Loader().define(instrument(writer.toByteArray(), "original.Overloaded"))
        type.getConstructor().newInstance()
        type.getConstructor(Int::class.javaPrimitiveType).newInstance(1)
        assertEquals(listOf("", "int"), InjectAsm.events.map { it.parameterClassNames })
    }

    private fun instrument(bytes: ByteArray, name: String): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        ClassReader(bytes).accept(InjectClassVisitor(writer, name), ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    private fun fixture(
        name: String,
        descriptor: String,
        parent: String = "java/lang/Object",
        injected: Boolean = true,
        throws: Boolean = false,
    ): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, name, null, parent, null)
        constructor(writer, descriptor, parent, injected, throws)
        writer.visitEnd()
        return writer.toByteArray()
    }

    private fun constructor(writer: ClassWriter, descriptor: String, parent: String, injected: Boolean, throws: Boolean) {
        val method = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null)
        if (injected) method.visitAnnotation("Ljavax/inject/Inject;", true).visitEnd()
        method.visitCode()
        method.visitVarInsn(Opcodes.ALOAD, 0)
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, parent, "<init>", "()V", false)
        if (throws) {
            method.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException")
            method.visitInsn(Opcodes.DUP)
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "()V", false)
            method.visitInsn(Opcodes.ATHROW)
        } else {
            method.visitInsn(Opcodes.RETURN)
        }
        method.visitMaxs(0, 0)
        method.visitEnd()
    }

    private class Loader : ClassLoader(DemeterInjectPluginTest::class.java.classLoader) {
        fun define(bytes: ByteArray): Class<*> = defineClass(null, bytes, 0, bytes.size)
    }
}