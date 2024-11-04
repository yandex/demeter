package com.yandex.demeter.profiler.compose

import android.content.Context
import android.view.View
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.compose.internal.data.ComposeMetricHolder
import com.yandex.demeter.profiler.compose.internal.ir.tracer.ComposeTracerMetricHolder
import com.yandex.demeter.profiler.compose.internal.ui.ComposePluginView
import kotlinx.coroutines.CoroutineScope

class ComposeDemeterPlugin : UiDemeterPlugin {
    override val id: String get() = "com.yandex.demeter.profiler.compose"
    override val name: String get() = "Compose"

    override fun init(consumerScope: CoroutineScope) {
        ComposeMetricHolder.init(consumerScope)
        ComposeTracerMetricHolder.init(consumerScope)
    }

    override fun ui(context: Context): View = ComposePluginView(context)
}
