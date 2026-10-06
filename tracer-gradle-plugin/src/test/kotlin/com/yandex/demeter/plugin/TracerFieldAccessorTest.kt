package com.yandex.demeter.plugin

import com.yandex.demeter.tracer.plugin.asm.TracerClassVisitor
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TracerFieldAccessorTest {
  @Test
  fun `ordinary instance and static field getters stay uninstrumented`() {
    for (descriptor in listOf("Z", "I", "J", "F", "D", "Ljava/lang/String;", "[I")) {
      for (isStatic in listOf(false, true)) {
        val flags = Opcodes.ACC_PUBLIC or if (isStatic) Opcodes.ACC_STATIC else 0
        val getter = transformGetter(descriptor = descriptor, methodAccess = flags)

        assertEquals(emptyList(), getter.calledMethods(), "$descriptor static=$isStatic")
        assertEquals(
          if (isStatic) {
            listOf(Opcodes.GETSTATIC, Type.getType(descriptor).getOpcode(Opcodes.IRETURN))
          } else {
            listOf(
              Opcodes.ALOAD,
              Opcodes.GETFIELD,
              Type.getType(descriptor).getOpcode(Opcodes.IRETURN)
            )
          },
          getter.instructions.toArray().filter { it.opcode >= 0 }.map { it.opcode }
        )
      }
    }
  }

  @Test
  fun `synchronized and volatile getters remain instrumented`() {
    assertTraced(
      transformGetter(
        descriptor = "I",
        methodAccess = Opcodes.ACC_PUBLIC or Opcodes.ACC_SYNCHRONIZED
      )
    )
    assertTraced(
      transformGetter(
        descriptor = "I",
        methodAccess = Opcodes.ACC_PUBLIC,
        extraFieldAccess = Opcodes.ACC_VOLATILE
      )
    )
  }

  @Test
  fun `foreign static field access remains instrumented`() {
    assertTraced(
      transformGetter(
        descriptor = "I",
        methodAccess = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
        fieldOwner = "com/yandex/test/Other"
      )
    )
  }

  @Test
  fun `instance getter of own static field remains instrumented`() {
    assertTraced(
      transformGetter(
        descriptor = "I",
        methodAccess = Opcodes.ACC_PUBLIC,
        fieldIsStatic = true
      )
    )
  }

  @Test
  fun `getter with computation remains instrumented`() {
    assertTraced(
      transformGetter(
        descriptor = "I",
        methodAccess = Opcodes.ACC_PUBLIC,
        compute = true
      )
    )
  }

  @Test
  fun `static initialization remains instrumented`() {
    val input = ClassWriter(ClassWriter.COMPUTE_MAXS).apply {
      visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC,
        OWNER,
        null,
        "java/lang/Object",
        null
      )
      visitMethod(
        Opcodes.ACC_STATIC,
        "<clinit>",
        "()V",
        null,
        null
      ).apply {
        visitCode()
        visitInsn(Opcodes.RETURN)
        visitMaxs(0, 0)
        visitEnd()
      }
      visitEnd()
    }
    assertTraced(transform(input.toByteArray()).methods.single())
  }

  private fun transformGetter(
    descriptor: String,
    methodAccess: Int,
    extraFieldAccess: Int = 0,
    fieldOwner: String = OWNER,
    compute: Boolean = false,
    fieldIsStatic: Boolean = methodAccess and Opcodes.ACC_STATIC != 0
  ): MethodNode {
    val input = ClassWriter(ClassWriter.COMPUTE_MAXS).apply {
      visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC,
        OWNER,
        null,
        "java/lang/Object",
        null
      )
      visitField(
        Opcodes.ACC_PRIVATE or extraFieldAccess or if (fieldIsStatic) Opcodes.ACC_STATIC else 0,
        "value",
        descriptor,
        null,
        null
      ).visitEnd()
      visitMethod(
        methodAccess,
        "getValue",
        "()$descriptor",
        null,
        null
      ).apply {
        visitCode()
        // Debug nodes must not change recognition of the executable getter instructions.
        val start = Label()
        visitLabel(start)
        visitLineNumber(1, start)
        if (!fieldIsStatic) {
          visitVarInsn(Opcodes.ALOAD, 0)
        }
        visitFieldInsn(
          if (fieldIsStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD,
          fieldOwner,
          "value",
          descriptor
        )
        if (compute) {
          visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/Math",
            "abs",
            "(I)I",
            false
          )
        }
        visitInsn(Type.getType(descriptor).getOpcode(Opcodes.IRETURN))
        visitMaxs(0, 0)
        visitEnd()
      }
      visitEnd()
    }
    return transform(input.toByteArray()).methods.single()
  }

  private fun transform(bytes: ByteArray): ClassNode {
    val output = ClassWriter(ClassWriter.COMPUTE_MAXS)
    ClassReader(bytes).accept(
      TracerClassVisitor(output, OWNER.replace('/', '.')),
      0
    )
    return ClassNode(ASM_API_VERSION).also {
      ClassReader(output.toByteArray()).accept(it, 0)
    }
  }

  private fun assertTraced(method: MethodNode) {
    val called = method.calledMethods()
    assertEquals(1, called.count { it == "nanoTime" })
    assertEquals(1, called.count { it == "beginSection" })
    assertEquals(1, called.count { it == "endSection" })
    assertTrue(method.instructions.toArray().any { it.opcode in Opcodes.IRETURN..Opcodes.RETURN })
  }

  private fun MethodNode.calledMethods(): List<String> =
    instructions.toArray().filterIsInstance<MethodInsnNode>().map { it.name }

  private companion object {
    const val OWNER = "com/yandex/test/AccessorFixture"
  }
}