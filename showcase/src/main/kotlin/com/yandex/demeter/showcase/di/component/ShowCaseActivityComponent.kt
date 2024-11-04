package com.yandex.demeter.showcase.di.component

import android.content.Context
import com.yandex.demeter.showcase.annotation.UiContextShowCase
import com.yandex.demeter.showcase.ui.DemeterShowCaseActivity
import dagger.BindsInstance
import dagger.Component

@Component
interface ShowCaseActivityComponent {
  fun inject(demeterShowCaseActivity: DemeterShowCaseActivity)

  @Component.Factory
  interface Factory {
    fun create(@BindsInstance @UiContextShowCase context: Context): ShowCaseActivityComponent
  }
}