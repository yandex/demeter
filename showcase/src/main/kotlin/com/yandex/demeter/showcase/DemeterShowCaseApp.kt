package com.yandex.demeter.showcase

import android.app.Application
import com.yandex.demeter.Demeter
import com.yandex.demeter.DemeterInitializer
import com.yandex.demeter.profiler.compose.ComposeDemeterPlugin
import com.yandex.demeter.profiler.inject.InjectDemeterPlugin
import com.yandex.demeter.profiler.tracer.TracerDemeterPlugin
import com.yandex.demeter.showcase.di.component.AppComponent
import com.yandex.demeter.showcase.di.component.DaggerAppComponent

class DemeterShowCaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Demeter.init(
            initializer = DemeterInitializer(
                context = this,
                plugins = listOf(
                    TracerDemeterPlugin(),
                    InjectDemeterPlugin(),
                    ComposeDemeterPlugin(),
                ),
            )
        )

        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}
