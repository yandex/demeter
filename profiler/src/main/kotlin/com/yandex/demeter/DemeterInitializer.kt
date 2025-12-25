package com.yandex.demeter

class DemeterInitializer(
    private val plugins: List<DemeterPlugin>,
) : Demeter.Initializer {
    override fun init(): Demeter.Core {
        return DemeterCoreInitializer.init(plugins)
    }
}
