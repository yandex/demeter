package com.yandex.demeter.profiler.coroutine.tracer.ui

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.DemeterReporter
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.profiler.coroutine.tracer.CoroutineTracerDemeterPlugin
import com.yandex.demeter.profiler.coroutine.tracer.ui.internal.CoroutineTracerScreen

class CoroutineTracerUiDemeterPlugin(
    context: Context,
    reporter: DemeterReporter? = null,
) : UiDemeterPlugin {

    @InternalDemeterApi
    override val name: String get() = "Coroutine Tracer"

    @InternalDemeterApi
    override val plugin: DemeterPlugin = CoroutineTracerDemeterPlugin(context.applicationContext, reporter)

    @InternalDemeterApi
    override fun ui(context: Context): View {
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                CoroutineTracerScreen()
            }
        }
    }
}
