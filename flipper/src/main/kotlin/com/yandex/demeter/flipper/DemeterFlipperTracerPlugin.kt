package com.yandex.demeter.flipper

import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.plugins.common.BufferingFlipperPlugin

object DemeterFlipperTracerPlugin : BufferingFlipperPlugin() {
    override fun getId(): String = "demeter-tracer"

    internal fun report(flipperObject: FlipperObject) {
        send("newRow", flipperObject)
    }
}