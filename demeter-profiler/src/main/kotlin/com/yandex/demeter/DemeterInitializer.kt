package com.yandex.demeter

import android.content.Context

class DemeterInitializer(
    private val context: Context,
    private val plugins: List<DemeterPlugin>,
) : Demeter.Initializer {
    override fun init(): Demeter.Core {
        val core = DemeterCoreInitializer.init(plugins)
        DemeterUiInitializer.init(context, plugins)
        return core
    }
}
