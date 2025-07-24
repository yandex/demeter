package com.yandex.demeter.flipper

import com.facebook.flipper.core.FlipperObject
import com.yandex.demeter.Reporter

object DemeterFlipperTraceReporter : Reporter {
    private const val DURATION_KEY = "ms"
    private const val FLIPPER_THRESHOLD_MS = 50L

    override fun report(payload: Map<String, Any>) {
        val lastDurationMs = payload[DURATION_KEY]?.toString()?.toLongOrNull() ?: return

        // do not spam flipper
        if (lastDurationMs >= FLIPPER_THRESHOLD_MS) {
            DemeterFlipperTracerPlugin.report(
                payload.keys.fold(FlipperObject.Builder()) { b, key -> b.put(key, payload[key]) }
                    .build()
            )
        }
    }
}