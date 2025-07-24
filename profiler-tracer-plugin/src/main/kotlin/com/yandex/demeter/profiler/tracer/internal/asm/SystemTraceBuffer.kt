package com.yandex.demeter.profiler.tracer.internal.asm

import android.os.Build
import android.os.Trace
import androidx.annotation.ChecksSdkIntAtLeast
import com.yandex.demeter.annotations.InternalDemeterApi

/**
 * Delegate for working with system trace buffer.
 */
@InternalDemeterApi
object SystemTraceBuffer {
    @get:ChecksSdkIntAtLeast(Build.VERSION_CODES.Q)
    inline val atLeastQ get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    @Suppress("NOTHING_TO_INLINE", "UnclosedTrace")
    inline fun beginSection(
        id: Long,
        fullMethodName: String,
    ) {
        if (atLeastQ) {
            Trace.beginAsyncSection(fullMethodName, id.toInt())
        } else {
            Trace.beginSection(fullMethodName)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun endSection(
        id: Long,
        fullMethodName: String,
    ) {
        if (atLeastQ) {
            Trace.endAsyncSection(fullMethodName, id.toInt())
        } else {
            Trace.endSection()
        }
    }
}
