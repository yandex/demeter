@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package com.yandex.demeter.showcase.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import com.yandex.demeter.showcase.di.component.DaggerShowCaseActivityComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import javax.inject.Inject

@Suppress("MagicNumber")
class DemeterShowCaseActivity : ComponentActivity() {

    @Inject
    lateinit var showCaseViewFactory: ShowCaseView.Factory

    private val scope = CoroutineScope(Dispatchers.Default)
    private val mainScope = MainScope()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerShowCaseActivityComponent
            .factory()
            .create(this)
            .inject(this)

        setContent {
            MaterialTheme {
                Column {
                    ShowCaseLayout(showCaseViewFactory = showCaseViewFactory)
                }
            }
        }
    }
}