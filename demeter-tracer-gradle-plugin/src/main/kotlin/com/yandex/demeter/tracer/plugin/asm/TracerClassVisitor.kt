package com.yandex.demeter.tracer.plugin.asm

import com.yandex.demeter.plugin.ASM_API_VERSION
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

private const val CLASS_METHOD_SEPARATOR = "#"

class TracerClassVisitor(
    classVisitor: ClassVisitor,
    private val className: String,
) : ClassVisitor(ASM_API_VERSION, classVisitor) {
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor? {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions) ?: return null
        return TracerMethodAdapter(className, mv, access, name, descriptor)
    }

    private class TracerMethodAdapter(
        private val className: String,
        private val methodVisitor: MethodVisitor,
        access: Int,
        name: String,
        descriptor: String,
    ) : AdviceAdapter(
        ASM_API_VERSION,
        methodVisitor,
        access,
        name,
        descriptor,
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
