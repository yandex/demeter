package com.yandex.demeter.showcase.di.component

import android.content.Context
import com.yandex.demeter.showcase.annotation.UiContextShowCase
import javax.inject.Inject

data class ShowCaseViewDependencies @Inject constructor(
    @UiContextShowCase @get:UiContextShowCase
    val context: Context
)