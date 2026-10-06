package com.yandex.demeter.tracer.plugin.asm

import com.yandex.demeter.plugin.ASM_API_VERSION
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

private const val CLASS_METHOD_SEPARATOR = "#"

class TracerClassVisitor(
  classVisitor: ClassVisitor,
  private val className: String
) : ClassVisitor(ASM_API_VERSION, classVisitor) {
  private var internalClassName = className.replace('.', '/')
  private val fieldAccess = hashMapOf<Pair<String, String>, Int>()

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<out String>?
  ) {
    internalClassName = name
    fieldAccess.clear()
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitField(
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    value: Any?
  ): FieldVisitor? {
    // ClassReader visits fields before methods; declaration flags let us reject volatile reads.
    fieldAccess[name to descriptor] = access
    return super.visitField(access, name, descriptor, signature, value)
  }

  override fun visitMethod(
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor? {
    val methodVisitor = super.visitMethod(
      access,
      name,
      descriptor,
      signature,
      exceptions
    ) ?: return null

    // Buffer the original bytecode: inserting trace calls first would hide the accessor pattern.
    return object : MethodNode(
      ASM_API_VERSION,
      access,
      name,
      descriptor,
      signature,
      exceptions
    ) {
      override fun visitEnd() {
        super.visitEnd()
        // Observable trace calls prevent R8 from treating field getters as simple accessors.
        val visitor = if (isTrivialFieldGetter(this, internalClassName, fieldAccess)) {
          methodVisitor
        } else {
          TracerMethodAdapter(
            className,
            methodVisitor,
            access,
            name,
            descriptor
          )
        }
        // Replay the method, including annotations, frames and debug metadata, exactly once.
        accept(visitor)
      }
    }
  }

  private class TracerMethodAdapter(
    private val className: String,
    private val methodVisitor: MethodVisitor,
    access: Int,
    name: String,
    descriptor: String
  ) : AdviceAdapter(
    ASM_API_VERSION,
    methodVisitor,
    access,
    name,
    descriptor
  ) {
    private val fullMethodName = "$className$CLASS_METHOD_SEPARATOR$name"
    private var startVarIndex = 0

    override fun visitCode() {
      super.visitCode()
      startVarIndex = newLocal(Type.LONG_TYPE)
      methodVisitor.visitMethodInsn(
        INVOKESTATIC,
        "java/lang/System",
        "nanoTime",
        "()J",
        false
      )
      methodVisitor.visitVarInsn(LSTORE, startVarIndex)
      methodVisitor.visitVarInsn(LLOAD, startVarIndex)
      methodVisitor.visitLdcInsn(fullMethodName)
      methodVisitor.visitMethodInsn(
        INVOKESTATIC,
        "com/yandex/demeter/profiler/tracer/internal/asm/TracerAsm",
        "beginSection",
        "(JLjava/lang/String;)V",
        false
      )
    }

    override fun onMethodExit(opcode: Int) {
      methodVisitor.visitVarInsn(LLOAD, startVarIndex)
      methodVisitor.visitLdcInsn(fullMethodName)
      methodVisitor.visitLdcInsn(className)
      methodVisitor.visitLdcInsn(name)
      methodVisitor.visitMethodInsn(
        INVOKESTATIC,
        "com/yandex/demeter/profiler/tracer/internal/asm/TracerAsm",
        "endSection",
        "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
        false
      )
    }
  }
}

internal fun isTrivialFieldGetter(
  method: MethodNode,
  owner: String,
  fields: Map<Pair<String, String>, Int>
): Boolean {
  // Access flags are a bit mask: a nonzero result means the flag is set.
  // Keep tracing synchronization and bodies with arguments or exception handlers.
  if (
    method.access and Opcodes.ACC_SYNCHRONIZED != 0 ||
    Type.getArgumentTypes(method.desc).isNotEmpty() ||
    method.tryCatchBlocks.isNotEmpty()
  ) {
    return false
  }

  val returnType = Type.getReturnType(method.desc)
  if (returnType == Type.VOID_TYPE) {
    return false
  }

  // ASM gives labels, frames and line numbers opcode -1. Only opcodes >= 0 execute on the JVM.
  val code = method.instructions.toArray()
    .filter { it.opcode >= 0 }
  val isStatic = method.access and Opcodes.ACC_STATIC != 0

  // Static getter:   [0] GETSTATIC, [1] typed return.
  // Instance getter: [0] ALOAD 0, [1] GETFIELD, [2] typed return.
  // The field read is first for a static method and follows the receiver load for an instance method.
  val fieldIndex = if (isStatic) 0 else 1
  // Two instructions starting at fieldIndex: the field read and its return. Reject any extra work.
  if (code.size != fieldIndex + 2) {
    return false
  }

  if (!isStatic) {
    val receiver = code[0] as? VarInsnNode ?: return false
    // Local variable slot 0 holds `this` in an instance method. A different slot reads another receiver.
    if (
      receiver.opcode != Opcodes.ALOAD ||
      receiver.`var` != 0
    ) {
      return false
    }
  }

  val field = code[fieldIndex] as? FieldInsnNode ?: return false
  // A static method initializes its own class before entry. GETSTATIC from an instance method
  // or another class may initialize a class inside the measured body, so keep tracing those accesses.
  val expectedAccess = if (isStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD
  if (
    field.opcode != expectedAccess ||
    field.owner != owner
  ) {
    return false
  }

  // Read declaration flags by name and descriptor; unknown fields are not safe to skip.
  val access = fields[field.name to field.desc] ?: return false
  val isStaticField = access and Opcodes.ACC_STATIC != 0
  // Volatile reads participate in synchronization. Also require the field kind to match GETSTATIC/GETFIELD.
  if (
    access and Opcodes.ACC_VOLATILE != 0 ||
    isStaticField != isStatic
  ) {
    return false
  }

  // IRETURN is ASM's base opcode; getOpcode selects the return instruction for the actual JVM type.
  return code.last().opcode == returnType.getOpcode(Opcodes.IRETURN)
}