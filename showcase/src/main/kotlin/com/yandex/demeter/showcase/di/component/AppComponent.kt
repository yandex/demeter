package com.yandex.demeter.showcase.di.component

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface AppComponent {
  @Component.Factory
  interface Factory {
    fun create(@BindsInstance app: Application): AppComponent
  }
}