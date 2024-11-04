package com.yandex.demeter

import com.yandex.demeter.internal.DemeterCore
import com.yandex.demeter.internal.demeterGlobalScope

object DemeterCoreInitializer {
    fun init(
        plugins: List<DemeterPlugin>,
    ): Demeter.Core {
        plugins.forEach { plugin ->
            plugin.init(
                consumerScope = demeterGlobalScope,
            )
        }

        return DemeterCore()
    }
}
