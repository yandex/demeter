package com.yandex.demeter.showcase.ui

import android.content.Context
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Stable
import com.yandex.demeter.showcase.annotation.UiContextShowCase
import com.yandex.demeter.showcase.di.component.DaggerShowCaseViewComponent
import com.yandex.demeter.showcase.di.component.ShowCaseViewDependencies
import com.yandex.demeter.showcase.di.scope.PerShowCase
import javax.inject.Inject

@PerShowCase
class ShowCaseView @Inject constructor(
    @UiContextShowCase context: Context,
    showCaseViewModel: ShowCaseViewModel
) : AppCompatTextView(context.apply { /* need to verify problem with code before invoke super constructor */ }) {

    @Stable
    data class Factory @Inject constructor(
        private val showCaseViewDependencies: ShowCaseViewDependencies
    ) {
        fun create(delay: Long): ShowCaseView {
            Thread.sleep(delay)
            return DaggerShowCaseViewComponent
                .factory()
                .create(showCaseViewDependencies)
                .showCaseView()
                .apply {
                    layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    text = "Delay was $delay ms"
                }
        }
    }
}