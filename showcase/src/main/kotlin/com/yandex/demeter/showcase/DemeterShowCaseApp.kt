package com.yandex.demeter.showcase

import android.app.Application
import android.util.Log
import com.yandex.demeter.Demeter
import com.yandex.demeter.UiDemeterInitializer
import com.yandex.demeter.profiler.compose.ui.ComposeUiDemeterPlugin
import com.yandex.demeter.profiler.inject.ui.InjectUiDemeterPlugin
import com.yandex.demeter.profiler.tracer.ui.TracerUiDemeterPlugin
import com.yandex.demeter.showcase.di.component.AppComponent
import com.yandex.demeter.showcase.di.component.DaggerAppComponent

private const val TAG = "ShowCaseApp"

class DemeterShowCaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Demeter.init(
            initializer = UiDemeterInitializer(
                context = this,
                uiPlugins = listOf(
                    TracerUiDemeterPlugin(
                        reporter = { payload -> Log.i(TAG, "[Tracer payload] $payload") }
                    ),
                    InjectUiDemeterPlugin(
                        reporter = { payload -> Log.i(TAG, "[Inject payload] $payload") }
                    ),
                    ComposeUiDemeterPlugin(),
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
