package com.yandex.demeter.profiler.inject

import android.content.Context
import android.view.View
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.inject.internal.data.AsmInjectMetricsHandler
import com.yandex.demeter.profiler.inject.internal.ui.InjectPluginView
import kotlinx.coroutines.CoroutineScope

class InjectDemeterPlugin : UiDemeterPlugin {

    @InternalDemeterApi
    override val id: String get() = "com.yandex.demeter.profiler.inject"

    @InternalDemeterApi
    override val name: String get() = "Inject"

    @InternalDemeterApi
    override fun init(consumerScope: CoroutineScope) {
        AsmInjectMetricsHandler.init(consumerScope)
    }

    @InternalDemeterApi
    override fun ui(context: Context): View = InjectPluginView(context)
}
