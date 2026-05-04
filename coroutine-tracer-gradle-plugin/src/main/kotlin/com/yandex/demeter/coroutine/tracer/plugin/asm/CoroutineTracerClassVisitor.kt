package com.yandex.demeter.coroutine.tracer.plugin.asm

import com.yandex.demeter.plugin.ASM_API_VERSION
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

private const val CLASS_METHOD_SEPARATOR = "#"

private const val COROUTINE_TRACER_ASM_OWNER =
    "com/yandex/demeter/profiler/coroutine/tracer/internal/asm/CoroutineTracerAsm"

private const val ON_COROUTINE_LAUNCHED_METHOD = "onCoroutineLaunched"

private const val ON_COROUTINE_LAUNCHED_DESCRIPTOR =
    "(Lkotlinx/coroutines/Job;Ljava/lang/String;)V"

class CoroutineTracerClassVisitor(
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
        return CoroutineTracerMethodAdapter(className, mv, access, name, descriptor)
    }

    private class CoroutineTracerMethodAdapter(
        private val className: String,
        methodVisitor: MethodVisitor,
        access: Int,
        private val methodName: String,
        descriptor: String,
    ) : AdviceAdapter(
        ASM_API_VERSION,
        methodVisitor,
        access,
        methodName,
        descriptor,
    ) {
        private var lastLineNumber: Int = -1

        override fun visitLineNumber(line: Int, start: Label?) {
            lastLineNumber = line
            super.visitLineNumber(line, start)
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean,
        ) {
            // Call the original method first
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

            // After a coroutine builder call, the Job/Deferred is on the stack as the return value.
            // We duplicate it and call our tracer to register the coroutine launch site.
            if (isCoroutineBuilderCall(owner, name)) {
                val launchSite = if (lastLineNumber > 0) {
                    "$className$CLASS_METHOD_SEPARATOR$methodName:$lastLineNumber"
                } else {
                    "$className$CLASS_METHOD_SEPARATOR$methodName"
                }
                // Stack: [..., Job]
                mv.visitInsn(DUP) // Stack: [..., Job, Job]
                mv.visitLdcInsn(launchSite) // Stack: [..., Job, Job, String]
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    COROUTINE_TRACER_ASM_OWNER,
                    ON_COROUTINE_LAUNCHED_METHOD,
                    ON_COROUTINE_LAUNCHED_DESCRIPTOR,
                    false
                )
                // Stack: [..., Job] (original return value preserved)
            }
        }

        private fun isCoroutineBuilderCall(owner: String, name: String): Boolean {
            if (name != "launch" && name != "async" &&
                name != "launch\$default" && name != "async\$default"
            ) {
                return false
            }
            return owner.startsWith("kotlinx/coroutines/BuildersKt")
        }
    }
}
