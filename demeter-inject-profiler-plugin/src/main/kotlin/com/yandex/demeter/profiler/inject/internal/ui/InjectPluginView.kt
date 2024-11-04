package com.yandex.demeter.profiler.inject.internal.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.yandex.demeter.Demeter
import com.yandex.demeter.internal.DemeterCore
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.interceptor.UiInterceptor
import com.yandex.demeter.internal.model.TimeMetricViewItem
import com.yandex.demeter.internal.utils.SortType
import com.yandex.demeter.internal.utils.shareCsv
import com.yandex.demeter.profiler.inject.databinding.InjectDemeterPluginViewBinding
import com.yandex.demeter.profiler.inject.internal.data.InjectMetricsRepository
import com.yandex.demeter.profiler.inject.internal.data.model.InjectMetric
import com.yandex.demeter.profiler.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * Represents Inject Plugin UI.
 */
internal class InjectPluginView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes,
) {
    private val binding: InjectDemeterPluginViewBinding =
        InjectDemeterPluginViewBinding.inflate(LayoutInflater.from(context), this)

    private val mainInterceptor = (Demeter.instance as DemeterCore).mainInterceptor
    private val interceptorList = (Demeter.instance as DemeterCore).interceptors
    private val threadFilterList = (Demeter.instance as DemeterCore).threadsFilters

    private val adapter = ExpandableMetricsListAdapter()

    private val scope = MainScope()
    private var job: Job? = null

    private var currentInterceptor: UiInterceptor

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        binding.timeFilter.visibility = View.VISIBLE
        binding.timeFilter.text = mainInterceptor.name
        binding.timeFilter.setOnClickListener { showMsInterceptorMenu() }

        val metricDescriptionsList = mainInterceptor.asExpandableMetricItem()

        binding.threadFilter.setOnClickListener { showThreadPicker() }
        binding.sort.setOnClickListener { showSortDialog() }
        binding.lvMetrics.setAdapter(adapter)

        currentInterceptor = mainInterceptor
        binding.timeFilter.text = "${mainInterceptor.name} (${metricDescriptionsList.size})"
        binding.export.setOnClickListener {
            job?.cancel()
            job = scope.launch(Dispatchers.IO) {
                shareCsv(
                    context = context,
                    metrics = InjectMetricsRepository.initializedMetrics.values,
                    logName = "inject",
                )
            }
        }
    }

    private fun showMsInterceptorMenu() {
        val transformerNames = arrayOfNulls<String>(interceptorList.size)
        for (i in interceptorList.indices) {
            transformerNames[i] = interceptorList[i].name
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.threshold_filter_dialog_title)
            .setItems(transformerNames) { _: DialogInterface?, which: Int ->
                for (i in 0 until adapter.groupCount) {
                    binding.lvMetrics.collapseGroup(i)
                }
                currentInterceptor = interceptorList[which]
                val list = currentInterceptor.asExpandableMetricItem()

                binding.timeFilter.text = "${interceptorList[which].name} (${list.size})"
                adapter.updateMetrics(list)
            }
        builder.create().show()
    }

    private fun showThreadPicker() {
        val transformerNames = arrayOfNulls<String>(
            threadFilterList.size
        )
        for (i in threadFilterList.indices) {
            transformerNames[i] = threadFilterList[i].name
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.thread_filter_dialog_title)
            .setItems(transformerNames) { _, which ->
                for (i in 0 until adapter.groupCount) {
                    binding.lvMetrics.collapseGroup(i)
                }
                val filterName = threadFilterList[which].name
                binding.threadFilter.text = filterName.ifEmpty {
                    context.getText(R.string.no_filter)
                }
                currentInterceptor = threadFilterList[which]
                adapter.updateMetrics(currentInterceptor.asExpandableMetricItem())
            }
        builder.create().show()
    }

    private fun showSortDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.sort_dialog_title)
            .setItems(
                arrayOf<CharSequence>(
                    SortType.ALPHABET.name,
                    SortType.TIME.name
                )
            ) { _: DialogInterface?, which: Int ->
                for (i in 0 until adapter.groupCount) {
                    binding.lvMetrics.collapseGroup(i)
                }

                job?.cancel()
                job = scope.launch {
                    val descriptions = withContext(Dispatchers.Default) {
                        currentInterceptor.asExpandableMetricItem().apply {
                            Collections.sort(this, SortType.entries[which].comparator)
                        }
                    }
                    adapter.updateMetrics(descriptions)
                }
            }
        builder.create().show()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val metricDescriptionsList = mainInterceptor.asExpandableMetricItem()
        adapter.updateMetrics(metricDescriptionsList)

        if (adapter.groupCount == 0) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.lvMetrics.visibility = View.GONE
            binding.tvEmpty.setText(R.string.no_collected_data)
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.lvMetrics.visibility = View.VISIBLE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    private fun UiInterceptor.asExpandableMetricItem(): List<TimeMetricViewItem> {
        return buildList {
            val displayList = intercept(ArrayList(InjectMetricsRepository.initializedMetrics.values))
            for (initMetric in displayList) {
                add(initMetric.asExpandableMetricItem())
            }
        }
    }

    private fun InjectMetric.asExpandableMetricItem(): TimeMetricViewItem {
        val warningLevelWithArgs = WarningLevel.getLevel(totalInitTime)

        val sb = with(StringBuilder("Initialization: ")) {
            append(initTime)
            append("ms, ")
            append("with args: ")
            if (warningLevelWithArgs != WarningLevel.Zero) {
                append("<b><font color='#9C27B0'>")
            }
            append(totalInitTime)
            append("ms")
            if (warningLevelWithArgs != WarningLevel.Zero) {
                append("</font></b>")
            }
            append(" <font color='#00BFA5'>($threadName)</font>")
        }.toString()

        return TimeMetricViewItem(
            simpleName = simpleName,
            description = sb,
            totalInitTime = initTime,
            args = args.provideDescriptionsTree(0, "")
        )
    }

    private fun List<InjectMetric>.provideDescriptionsTree(
        depthLevel: Int,
        prev: String,
    ): List<TimeMetricViewItem> {
        if (depthLevel == 0 && isEmpty()) {
            return listOf(
                TimeMetricViewItem(description = "No args or args initialized before")
            )
        }

        val items = mutableListOf<TimeMetricViewItem>()
        for ((count, initMetric) in withIndex()) {
            val initTimeWithoutArgs = initMetric.initTime
            val totalInitTime = initMetric.totalInitTime

            var depthStr = prev + "\u2502" + space(1)
            var edgeChar = "\u251c"
            var secondRowChar = "\u2502"

            if (count == size - 1) {
                edgeChar = "\u2514"
                secondRowChar = space(1)
                depthStr = prev + space(2)
            }

            val sb = with(StringBuilder(prev)) {
                append("$edgeChar\u2500\u25CF <b>")
                append(initMetric.simpleName)
                append("</b><br/>")
                append(prev + secondRowChar + space(1))
                append(initTimeWithoutArgs)
                append("ms, with args: ")
                append(totalInitTime)
                append("ms")
                append(" <font color='#00BFA5'>(" + initMetric.threadName + ")</font>")
            }.toString()

            items.addAll(
                listOf(
                    TimeMetricViewItem(
                        description = sb,
                        totalInitTime = totalInitTime
                    )
                ) +
                    if (initMetric.args.isNotEmpty()) {
                        initMetric.args.provideDescriptionsTree(depthLevel + 1, depthStr)
                    } else {
                        listOf()
                    }
            )
        }
        return items
    }

    private fun space(count: Int): String {
        var spaceChar = "\u2000\u2006"
        for (i in 0 until count - 1) {
            spaceChar += spaceChar
        }
        return spaceChar
    }
}
