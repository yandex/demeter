package com.yandex.demeter.coroutine.tracer.plugin.asm

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.yandex.demeter.plugin.isKtIntrinsics
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.TraceClassVisitor
import com.yandex.demeter.coroutine.tracer.plugin.asm.CoroutineTracerClassVisitorFactory.CoroutineTracerParams
import java.io.PrintWriter

abstract class CoroutineTracerClassVisitorFactory : AsmClassVisitorFactory<CoroutineTracerParams> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return if (isAsmDebug()) {
            TraceClassVisitor(nextClassVisitor, ASMifier(), PrintWriter(System.out))
        } else {
            CoroutineTracerClassVisitor(nextClassVisitor, classContext.currentClassData.className)
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className

        if (className.startsWith("java.") ||
            className.startsWith("javax.") ||
            className.startsWith("kotlin.") ||
            className.startsWith("kotlinx.")
        ) {
            return false
        }

        if (className.startsWith("com.yandex.demeter") &&
            !className.startsWith("com.yandex.demeter.showcase")
        ) {
            return false
        }

        if (classData.isKtIntrinsics) {
            return false
        }

        val includedClasses = parameters.get().includedClasses.getOrElse(emptyList())
        val excludedClasses = parameters.get().excludedClasses.getOrElse(emptyList())

        if (includedClasses.isEmpty()) {
            return false
        }

        return includedClasses.any { className.startsWith(it) } &&
            excludedClasses.none { className.startsWith(it) }
    }

    private fun isAsmDebug(): Boolean {
        return parameters.get().asmDebug.getOrElse(false)
    }

    interface CoroutineTracerParams : InstrumentationParameters {
        @get:Internal
        val asmDebug: Property<Boolean>

        @get:Internal
        val includedClasses: ListProperty<String>

        @get:Internal
        val excludedClasses: ListProperty<String>
    }
}
