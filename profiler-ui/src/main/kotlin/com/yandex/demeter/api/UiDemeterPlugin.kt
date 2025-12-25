package com.yandex.demeter.api

import android.content.Context
import android.view.View
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.annotations.InternalDemeterApi

interface UiDemeterPlugin {
    @InternalDemeterApi
    val name: String

    @InternalDemeterApi
    val plugin: DemeterPlugin

    @InternalDemeterApi
    fun ui(context: Context): View
}
