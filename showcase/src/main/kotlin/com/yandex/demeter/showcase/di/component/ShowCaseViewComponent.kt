package com.yandex.demeter.showcase.di.component

import com.yandex.demeter.showcase.di.scope.PerShowCase
import com.yandex.demeter.showcase.ui.ShowCaseView
import dagger.Component

@PerShowCase
@Component(dependencies = [ShowCaseViewDependencies::class])
interface ShowCaseViewComponent {

  fun showCaseView(): ShowCaseView

  @Component.Factory
  interface Factory {
    fun create(showCaseViewDependencies: ShowCaseViewDependencies): ShowCaseViewComponent
  }
}