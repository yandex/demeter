package com.yandex.demeter.tracer.plugin.asm

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
import com.yandex.demeter.tracer.plugin.asm.TracerClassVisitorFactory.TracerParams
import java.io.PrintWriter

abstract class TracerClassVisitorFactory : AsmClassVisitorFactory<TracerParams> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return if (isAsmDebug()) {
            TraceClassVisitor(nextClassVisitor, ASMifier(), PrintWriter(System.out))
        } else {
            TracerClassVisitor(nextClassVisitor, classContext.currentClassData.className)
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return parameters.get().includedClasses.getOrElse(emptyList())
            .any { classData.className.startsWith(it) }
            && parameters.get().excludedClasses.getOrElse(emptyList())
            .none { classData.className.startsWith(it) }
            && !classData.isKtIntrinsics
            && (!classData.className.startsWith("com.yandex.demeter")
            || classData.className.startsWith("com.yandex.demeter.showcase"))
    }

    private fun isAsmDebug(): Boolean {
        return parameters.get().asmDebug.getOrElse(false)
    }

    interface TracerParams : InstrumentationParameters {
        @get:Internal
        val asmDebug: Property<Boolean>

        @get:Internal
        val includedClasses: ListProperty<String>

        @get:Internal
        val excludedClasses: ListProperty<String>
    }
}
