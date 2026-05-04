package com.yandex.demeter.internal.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.core.UiConfig
import com.yandex.demeter.profiler.ui.R

@InternalDemeterApi
class MetricsActivity : AppCompatActivity() {

    private lateinit var vpMetrics: ViewPager2
    private val adapter = MetricsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.adm_activity_metrics)

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            insets
        }

        vpMetrics = findViewById(R.id.vpMetrics)
        vpMetrics.adapter = adapter
        adapter.updateTabs(UiConfig.plugins)

        TabLayoutMediator(findViewById(R.id.tabLayout), vpMetrics) { tab, position ->
            tab.text = UiConfig.plugins[adapter.getItemViewType(position)].name
            vpMetrics.setCurrentItem(tab.position, true)
        }.attach()
    }
}
