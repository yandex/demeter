package com.yandex.demeter.profiler.inject.ui

import android.content.Context
import android.view.View
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.inject.InjectDemeterPlugin
import com.yandex.demeter.profiler.inject.ui.internal.InjectPluginView

class InjectUiDemeterPlugin(
    reporter: DemeterReporter? = null
) : UiDemeterPlugin {

    @InternalDemeterApi
    override val name: String get() = "Inject"

    @InternalDemeterApi
    override val plugin: DemeterPlugin = InjectDemeterPlugin(reporter)

    @InternalDemeterApi
    override fun ui(context: Context): View = InjectPluginView(context)
}
