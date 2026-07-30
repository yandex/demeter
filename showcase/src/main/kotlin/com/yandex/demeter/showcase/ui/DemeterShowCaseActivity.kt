@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package com.yandex.demeter.showcase.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.yandex.demeter.showcase.DemeterShowCaseApp
import com.yandex.demeter.showcase.di.component.DaggerShowCaseActivityComponent
import com.yandex.demeter.showcase.feature.FeatureLayout
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
    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            showMetricsNotification()
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        DaggerShowCaseActivityComponent
            .factory()
            .create(this)
            .inject(this)

        setContent {
            MaterialTheme {
                Column(modifier = Modifier.systemBarsPadding()) {
                    FeatureLayout()
                    ShowCaseLayout(showCaseViewFactory = showCaseViewFactory)
                }
            }
        }

        requestMetricsNotification()
    }

    private fun requestMetricsNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showMetricsNotification()
        } else if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            showMetricsNotification()
        } else {
            notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showMetricsNotification() {
        (application as DemeterShowCaseApp).demeterInitializer.showMetricsNotification()
    }
}
