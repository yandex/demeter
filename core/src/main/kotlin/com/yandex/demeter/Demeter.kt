package com.yandex.demeter

object Demeter {
    private var initializer: Initializer? = null

    @JvmStatic
    val instance: Core by lazy {
        val initFunction = initializer ?: error("You must call init(...) first")
        initFunction.init()
    }

    @JvmStatic
    fun init(initializer: Initializer): Core {
        if (Demeter.initializer != null) {
            return instance
        }

        Demeter.initializer = initializer
        return instance
    }

    interface Core

    interface Initializer {
        fun init(): Core = object : Core {}
    }
}
