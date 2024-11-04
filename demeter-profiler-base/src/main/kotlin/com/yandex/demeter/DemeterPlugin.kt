package com.yandex.demeter

import com.yandex.demeter.annotations.InternalDemeterApi
import kotlinx.coroutines.CoroutineScope

interface DemeterPlugin {
    @InternalDemeterApi
    val id: String

    @InternalDemeterApi
    fun init(consumerScope: CoroutineScope)
}
