package com.yandex.demeter.profiler.compose.ui

import android.content.Context
import android.view.View
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.compose.ComposeDemeterPlugin
import com.yandex.demeter.profiler.compose.ui.internal.ComposePluginView

class ComposeUiDemeterPlugin : UiDemeterPlugin {
    @InternalDemeterApi
    override val name: String get() = "Compose"

    @InternalDemeterApi
    override val plugin: DemeterPlugin = ComposeDemeterPlugin()

    @InternalDemeterApi
    override fun ui(context: Context): View = ComposePluginView(context)
}
