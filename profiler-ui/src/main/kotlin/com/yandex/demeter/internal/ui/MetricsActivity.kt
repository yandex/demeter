package com.yandex.demeter.internal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.adm_activity_metrics)

        vpMetrics = findViewById(R.id.vpMetrics)
        vpMetrics.adapter = adapter
        adapter.updateTabs(UiConfig.plugins)

        TabLayoutMediator(findViewById(R.id.tabLayout), vpMetrics) { tab, position ->
            tab.text = UiConfig.plugins[adapter.getItemViewType(position)].name
            vpMetrics.setCurrentItem(tab.position, true)
        }.attach()
    }
}
