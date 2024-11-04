package com.yandex.demeter.profiler.tracer.internal.ui

import android.app.AlertDialog.Builder
import android.content.Context
import android.content.DialogInterface
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.utils.ComparableItemListImpl
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerView
import com.yandex.demeter.Demeter
import com.yandex.demeter.internal.DemeterCore
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.interceptor.UiInterceptor
import com.yandex.demeter.internal.utils.SortType
import com.yandex.demeter.internal.utils.SortType.ALPHABET
import com.yandex.demeter.internal.utils.SortType.TIME
import com.yandex.demeter.internal.utils.shareCsv
import com.yandex.demeter.profiler.tracer.databinding.TracerDemeterPluginViewBinding
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsRepository
import com.yandex.demeter.profiler.tracer.internal.data.model.TraceMetric
import com.yandex.demeter.profiler.ui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Represents Tracer Plugin UI.
 */
internal class TracerPluginView @JvmOverloads constructor(
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

    private val binding: TracerDemeterPluginViewBinding =
        TracerDemeterPluginViewBinding.inflate(LayoutInflater.from(context), this)

    private val lvMetrics = binding.lvMetrics
    private val tvEmpty = binding.tvEmpty
    private val interceptorList = (Demeter.instance as DemeterCore).interceptors
    private val mainInterceptor = (Demeter.instance as DemeterCore).mainInterceptor

    private var comparator: Comparator<TraceMetricViewItem> = TimeComparatorDescending()

    private val comparableListItem = ComparableItemListImpl(comparator)
    private val adapter = ItemAdapter(comparableListItem)

    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        binding.btnMenu.apply {
            text = mainInterceptor.name
            setOnClickListener { showMsInterceptorMenu() }
        }
        binding.sort.setOnClickListener {
            showSortDialog()
        }

        lvMetrics.layoutManager = LinearLayoutManager(context)
        lvMetrics.adapter = FastAdapter.with(adapter)
        lvMetrics.itemAnimator = null
        lvMetrics.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.export.setOnClickListener {
            job?.cancel()
            job = scope.launch(Dispatchers.IO) {
                shareCsv(
                    binding.root.context,
                    TraceMetricsRepository.metrics.values,
                    "tracer"
                )
            }
        }
        binding.fastscroller.apply {
            setupWithRecyclerView(
                binding.lvMetrics,
                { position ->
                    val item = adapter.getAdapterItem(position)
                    FastScrollItemIndicator.Text(item.name.substring(0, 1).uppercase())
                },
                useDefaultScroller = false
            )
            itemIndicatorSelectedCallbacks += object :
                FastScrollerView.ItemIndicatorSelectedCallback {
                override fun onItemIndicatorSelected(
                    indicator: FastScrollItemIndicator,
                    indicatorCenterY: Int,
                    itemPosition: Int
                ) {
                    binding.lvMetrics.apply {
                        scrollToPosition(itemPosition)
                    }
                }
            }
        }
        binding.fastscrollerThumb.setupWithFastScroller(binding.fastscroller)
    }

    private fun showMsInterceptorMenu() {
        val transformerNames = arrayOfNulls<String>(interceptorList.size)
        for (i in interceptorList.indices) {
            transformerNames[i] = interceptorList[i].name
        }
        val builder = Builder(context).apply {
            setTitle(R.string.settings)
            setItems(transformerNames) { _, which: Int ->
                binding.btnMenu.text = interceptorList[which].name
                adapter.setNewList(interceptorList[which].asTraceMetrics())
            }
        }
        builder.create().show()
    }

    private fun showSortDialog() {
        Builder(context)
            .setTitle(R.string.sort_dialog_title)
            .setItems(
                arrayOf<CharSequence>(
                    ALPHABET.name,
                    TIME.name
                )
            ) { _: DialogInterface?, which: Int ->
                job?.cancel()
                job = scope.launch {
                    comparator = when (SortType.entries[which]) {
                        ALPHABET -> {
                            binding.fastscroller.visibility = VISIBLE
                            AlphabetComparatorAscending()
                        }

                        TIME -> {
                            binding.fastscroller.visibility = GONE
                            TimeComparatorDescending()
                        }
                    }
                    comparableListItem.withComparator(comparator, sortNow = true)
                }
            }

            .create()
            .show()
    }

    private inner class AlphabetComparatorAscending : Comparator<TraceMetricViewItem> {
        override fun compare(lhs: TraceMetricViewItem, rhs: TraceMetricViewItem): Int {
            return lhs.name.compareTo(rhs.name)
        }
    }

    private inner class TimeComparatorDescending : Comparator<TraceMetricViewItem> {
        override fun compare(lhs: TraceMetricViewItem, rhs: TraceMetricViewItem): Int {
            return rhs.time.compareTo(lhs.time)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter.setNewList(mainInterceptor.asTraceMetrics(), false)
        invalidate()
    }

    override fun invalidate() {
        if (adapter.adapterItemCount == 0) {
            tvEmpty.visibility = VISIBLE
            lvMetrics.visibility = GONE
            tvEmpty.setText(R.string.no_collected_data)
        } else {
            tvEmpty.visibility = GONE
            lvMetrics.visibility = VISIBLE
        }
    }

    private fun UiInterceptor.asTraceMetrics(): List<TraceMetricViewItem> {
        return intercept(TraceMetricsRepository.metrics.values.toList())
            .map { it.asViewItem() }
            .toList()
    }

    private fun TraceMetric.asViewItem(): TraceMetricViewItem {
        val (background, textColor) = when (WarningLevel.getLevel(maxTime)) {
            WarningLevel.Zero -> R.color.d2m_transparent to R.color.d2m_font_default_description
            WarningLevel.First -> R.color.d2m_bg_warning_1 to R.color.d2m_font_warning_1_and_2
            WarningLevel.Second -> R.color.d2m_bg_warning_2 to R.color.d2m_font_warning_1_and_2
            WarningLevel.Third -> R.color.d2m_bg_warning_3 to R.color.d2m_font_warning_3
        }
        return TraceMetricViewItem(
            name = simpleName,
            description = colorizeDescription(),
            time = maxTime,
            backgroundRes = background,
            textColor = textColor,
        )
    }

    private fun TraceMetric.colorizeDescription(): String {
        val warningLevel = WarningLevel.getLevel(maxTime)
        return with(StringBuilder("Max time: ")) {
            if (warningLevel != WarningLevel.Zero) {
                append("<b><font color='#9C27B0'>")
            }
            append(maxTime)
            append("ms, ")
            if (warningLevel != WarningLevel.Zero) {
                append("</font></b>")
            }
            append("entered ")
            append(count)
            append(" times")
            append(" <font color='#00BFA5'>($threadName)</font>")
        }.toString()
    }

    private class TraceMetricViewItem(
        val name: String,
        val description: String,
        val time: Long,
        @ColorRes val backgroundRes: Int,
        @ColorRes val textColor: Int
    ) : AbstractItem<TraceMetricViewItem.ViewHolder>() {

        override val type: Int = R.id.trace_metrics_item_id

        override val layoutRes: Int = R.layout.adm_list_item_trace_metrics

        override fun getViewHolder(v: View) = ViewHolder(v)

        private class ViewHolder(view: View) : FastAdapter.ViewHolder<TraceMetricViewItem>(view) {
            var name: TextView = view.findViewById(R.id.tvClassName)
            var description: TextView = view.findViewById(R.id.tvInitTime)

            override fun bindView(item: TraceMetricViewItem, payloads: List<Any>) {
                name.text = item.name
                description.text = Html.fromHtml(item.description)
                itemView.setBackgroundResource(item.backgroundRes)
                description.setTextColor(itemView.resources.getColor(item.textColor))
            }

            override fun unbindView(item: TraceMetricViewItem) {
                name.text = null
                description.text = null
            }
        }
    }
}
