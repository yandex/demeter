package com.yandex.demeter.inject.plugin.asm

import com.yandex.demeter.plugin.ASM_API_VERSION
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class InjectClassVisitor(
    classVisitor: ClassVisitor?,
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
        return InjectMethodAdapter(className, mv, access, name, descriptor)
    }

    private class InjectMethodAdapter(
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
        private var isInjectConstructor = false
        private var startVarIndex = 0

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if ("<init>" == name && "Ljavax/inject/Inject;" == descriptor) {
                isInjectConstructor = true
            }
            return super.visitAnnotation(descriptor, visible)
        }

        override fun visitCode() {
            super.visitCode()
            if (!isInjectConstructor) {
                return
            }

            startVarIndex = newLocal(Type.LONG_TYPE)
            methodVisitor.visitMethodInsn(
                INVOKESTATIC,
                "java/lang/System",
                "nanoTime",
                "()J",
                false
            )
            methodVisitor.visitVarInsn(LSTORE, startVarIndex)
        }

        override fun onMethodExit(opcode: Int) {
            if (!isInjectConstructor) {
                return
            }

            methodVisitor.visitVarInsn(LLOAD, startVarIndex)
            methodVisitor.visitLdcInsn(className)
            methodVisitor.visitVarInsn(ALOAD, 0)
            methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/Object",
                "getClass",
                "()Ljava/lang/Class;",
                false
            )
            methodVisitor.visitMethodInsn(
                INVOKESTATIC,
                "com/yandex/demeter/profiler/inject/internal/asm/InjectAsm",
                "log",
                "(JLjava/lang/String;Ljava/lang/Class;)V",
                false
            )
        }
    }
}
