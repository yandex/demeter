package com.yandex.demeter.api

import android.content.Context
import android.view.View
import com.yandex.demeter.DemeterPlugin
import com.yandex.demeter.annotations.InternalDemeterApi

interface UiDemeterPlugin : DemeterPlugin {
    @InternalDemeterApi
    val name: String

    @InternalDemeterApi
    fun ui(context: Context): View
}
