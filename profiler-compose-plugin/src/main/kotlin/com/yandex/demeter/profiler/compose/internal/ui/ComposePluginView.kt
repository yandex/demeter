package com.yandex.demeter.profiler.compose.internal.ui

import android.app.AlertDialog.Builder
import android.content.Context
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
import com.yandex.demeter.profiler.compose.databinding.ComposeDemeterPluginViewBinding
import com.yandex.demeter.profiler.compose.internal.data.model.ComposeMetric
import com.yandex.demeter.profiler.compose.internal.data.ComposeMetricsValues
import com.yandex.demeter.profiler.ui.R

internal class ComposePluginView @JvmOverloads constructor(
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
    private val binding: ComposeDemeterPluginViewBinding =
        ComposeDemeterPluginViewBinding.inflate(LayoutInflater.from(context), this)

    private val lvMetrics = binding.lvMetrics
    private val tvEmpty = binding.tvEmpty

    private var comparator: Comparator<StateObjectMetricViewItem> = TimeComparatorAscending()
    private val comparableListItem = ComparableItemListImpl(comparator)
    private val adapter = ItemAdapter(comparableListItem)
    private var showRecompositions = true

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        lvMetrics.layoutManager = LinearLayoutManager(context)
        lvMetrics.adapter = FastAdapter.with(adapter)
        lvMetrics.itemAnimator = null
        lvMetrics.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        binding.fastscroller.apply {
            setupWithRecyclerView(
                binding.lvMetrics,
                { position ->
                    val item = adapter.getAdapterItem(position)
                    FastScrollItemIndicator.Text(item.title.substring(0, 1).uppercase())
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
        binding.btnFilter.setOnClickListener { showFilterPicker() }
    }

    private fun showFilterPicker() {
        val transformerNames = listOf(
            "Show/Hide recompositions",
        ).toTypedArray()
        Builder(context).apply {
            setTitle(R.string.compose_filter_title)
            setItems(transformerNames) { _, which: Int ->
                val filter: (ComposeMetric) -> Boolean = {
                    if (showRecompositions) {
                        true
                    } else {
                        it !is ComposeMetric.Recomposed
                    }
                }
                showRecompositions = !showRecompositions
                adapter.setNewList(fetchMetrics(filter), true)
            }
        }.create().show()
    }

    private inner class TimeComparatorAscending : Comparator<StateObjectMetricViewItem> {
        override fun compare(lhs: StateObjectMetricViewItem, rhs: StateObjectMetricViewItem): Int {
            return lhs.timestamp.compareTo(rhs.timestamp)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter.setNewList(
            fetchMetrics(),
            false
        )
        invalidate()
    }

    private fun fetchMetrics(filter: (ComposeMetric) -> Boolean = { true }): List<StateObjectMetricViewItem> {
        return ComposeMetricsValues.composeMetricsAsList
            .filter { filter(it) && it !is ComposeMetric.Skipped }
            .map { it.asViewItem() }
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

    private fun ComposeMetric.asViewItem(): StateObjectMetricViewItem {
        return when (this) {
            is ComposeMetric.Changed -> {
                val change = "'${change.prevValue}' -> '${change.newValue}'".colorized()
                StateObjectMetricViewItem(
                    timestamp = timestamp,
                    prefix = "[C]",
                    title = "${composition.stateName}: $change",
                    description = "${composition.composableFunctionName.colorized()} (${composition.fileNameWithPackage})",
                    backgroundRes = R.color.d2m_transparent,
                    textColor = R.color.d2m_font_default_description,
                )
            }

            is ComposeMetric.Forgotten -> StateObjectMetricViewItem(
                timestamp = timestamp,
                prefix = "[F]",
                title = composition.stateName,
                description = "${composition.composableFunctionName} (${composition.fileNameWithPackage})",
                backgroundRes = R.color.d2m_bg_warning_1,
                textColor = R.color.d2m_font_warning_1_and_2,
            )

            is ComposeMetric.Recomposed -> {
                val title = buildString {
                    if (recomposition.composableFunctionName.isNotBlank()) {
                        append(recomposition.composableFunctionName.colorized())
                    } else {
                        append(recomposition.fileNameWithPackage)
                    }
                }
                val description = recomposition.fileNameWithPackage
                    .takeIf { recomposition.composableFunctionName.isNotBlank() }

                StateObjectMetricViewItem(
                    timestamp = timestamp,
                    prefix = "[R]",
                    title = title,
                    description = description,
                    backgroundRes = R.color.d2m_bg_warning_3,
                    textColor = R.color.d2m_font_warning_3,
                )
            }

            is ComposeMetric.Remembered -> StateObjectMetricViewItem(
                timestamp = timestamp,
                prefix = "[N]",
                title = "${composition.stateName}: '${
                    change.newValue.toString().colorized()
                }'",
                description = "${composition.composableFunctionName} (${composition.fileNameWithPackage})",
                backgroundRes = R.color.d2m_bg_warning_2,
                textColor = R.color.d2m_font_warning_1_and_2,
            )

            is ComposeMetric.Skipped -> throw IllegalStateException("Not supported to display")
        }
    }

    private fun String.colorized(): String {
        return with(StringBuilder()) {
            append("<b><font color='#9C27B0'>${this@colorized}</font></b>")
        }.toString()
    }

    private class StateObjectMetricViewItem(
        val timestamp: Long,
        val prefix: String,
        val title: String,
        val description: String?,
        @ColorRes val backgroundRes: Int,
        @ColorRes val textColor: Int,
    ) : AbstractItem<StateObjectMetricViewItem.ViewHolder>() {

        override val type: Int = R.id.trace_metrics_item_id

        override val layoutRes: Int = R.layout.adm_list_item_state_object_metrics

        override fun getViewHolder(v: View) = ViewHolder(v)

        private class ViewHolder(view: View) :
            FastAdapter.ViewHolder<StateObjectMetricViewItem>(view) {
            var title: TextView = view.findViewById(R.id.tvTitle)
            var description: TextView = view.findViewById(R.id.tvDescription)
            var prefix: TextView = view.findViewById(R.id.tvPrefix)

            override fun bindView(item: StateObjectMetricViewItem, payloads: List<Any>) {
                val textColor = itemView.resources.getColor(item.textColor)

                description.visibility = if (item.description == null) {
                    GONE
                } else {
                    VISIBLE
                }

                title.text = Html.fromHtml(item.title)
                description.text = item.description?.let { Html.fromHtml(it) }
                prefix.text = item.prefix

                itemView.setBackgroundResource(item.backgroundRes)

                prefix.setTextColor(textColor)
                title.setTextColor(textColor)
                description.setTextColor(textColor)
            }

            override fun unbindView(item: StateObjectMetricViewItem) {
                title.text = null
                description.text = null
                prefix.text = null
            }
        }
    }
}
