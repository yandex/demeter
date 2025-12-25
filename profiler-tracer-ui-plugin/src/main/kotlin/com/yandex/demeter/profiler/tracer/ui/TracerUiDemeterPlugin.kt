package com.yandex.demeter.profiler.tracer.ui

import android.content.Context
import android.view.View
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.tracer.TracerDemeterPlugin
import com.yandex.demeter.profiler.tracer.ui.internal.TracerPluginView

class TracerUiDemeterPlugin(
    reporter: DemeterReporter? = null,
) : UiDemeterPlugin {
    @InternalDemeterApi
    override val name: String get() = "Tracer"

    @InternalDemeterApi
    override val plugin: DemeterPlugin = TracerDemeterPlugin(reporter)

    @InternalDemeterApi
    override fun ui(context: Context): View = TracerPluginView(context)
}
