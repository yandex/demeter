package com.yandex.demeter.profiler.tracer.ui.internal

import android.app.AlertDialog.Builder
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerView
import com.yandex.demeter.Demeter
import com.yandex.demeter.internal.DemeterCore
import com.yandex.demeter.internal.interceptor.UiInterceptor
import com.yandex.demeter.internal.utils.SortType
import com.yandex.demeter.internal.utils.SortType.ALPHABET
import com.yandex.demeter.internal.utils.SortType.TIME
import com.yandex.demeter.internal.utils.shareRawCsv
import com.yandex.demeter.internal.utils.shareFlameGraph
import com.yandex.demeter.internal.utils.shareTrace
import com.yandex.demeter.profiler.tracer.ui.databinding.TracerDemeterPluginViewBinding
import com.yandex.demeter.profiler.tracer.internal.data.TraceMetricsRepositoryImpl
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricEntity
import com.yandex.demeter.profiler.tracer.internal.data.db.asRawTraceMetrics
import com.yandex.demeter.profiler.tracer.internal.data.db.asTimeMetrics
import com.yandex.demeter.profiler.ui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    private val mainFilter = (Demeter.instance as DemeterCore).mainInterceptor

    private val interceptorEventsList = (Demeter.instance as DemeterCore).interceptors
    private var appliedEventsFilter: Int? = null

    private val threadsFilters = (Demeter.instance as DemeterCore).threadsFilters
    private var appliedThreadFilter: Int? = null

    private var currentSortType: SortType = TIME

    private val pagingAdapter = TraceMetricPagingAdapter()

    private val repository = TraceMetricsRepositoryImpl.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var metricsJob: Job? = null
    private var exportJob: Job? = null

    private val hasFiltersApplied: Boolean
        get() = appliedEventsFilter != null || appliedThreadFilter != null

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        binding.btnMenu.apply {
            text = mainFilter.name
            setOnClickListener {
                showFilterDialog(interceptorEventsList) { which ->
                    appliedEventsFilter = which
                    text = interceptorEventsList[which].name
                    observeMetrics()
                }
            }
        }
        binding.threadFilter.apply {
            text = appliedThreadFilter?.let { threadsFilters[it].name } ?: "all threads"
            setOnClickListener {
                showFilterDialog(threadsFilters) { which ->
                    appliedThreadFilter = which
                    text = threadsFilters[which].name
                    observeMetrics()
                }
            }
        }
        binding.sort.setOnClickListener {
            showSortDialog()
        }

        lvMetrics.layoutManager = LinearLayoutManager(context)
        lvMetrics.adapter = pagingAdapter
        lvMetrics.itemAnimator = null
        lvMetrics.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.export.setOnClickListener {
            showExportFormatDialog()
        }
        binding.fastscroller.apply {
            setupWithRecyclerView(
                binding.lvMetrics,
                { position ->
                    val name = pagingAdapter.getItemName(position)
                    if (name != null) {
                        FastScrollItemIndicator.Text(name.first().uppercaseChar().toString())
                    } else {
                        FastScrollItemIndicator.Text("")
                    }
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
                    binding.lvMetrics.scrollToPosition(itemPosition)
                }
            }
        }
        binding.fastscrollerThumb.setupWithFastScroller(binding.fastscroller)

        pagingAdapter.addLoadStateListener {
            updateEmptyState()
        }
    }

    private fun observeMetrics() {
        metricsJob?.cancel()
        metricsJob = scope.launch {
            if (hasFiltersApplied) {
                repository.getMetricsFlow(currentSortType)
                    .map { metrics -> applyFilters(metrics) }
                    .flowOn(Dispatchers.Default)
                    .collectLatest { filteredMetrics ->
                        pagingAdapter.submitData(PagingData.from(filteredMetrics))
                    }
            } else {
                repository.getMetricsPaged(currentSortType)
                    .collectLatest { pagingData ->
                        pagingAdapter.submitData(pagingData)
                    }
            }
        }
    }

    private fun applyFilters(metrics: List<TraceMetricEntity>): List<TraceMetricEntity> {
        var wrappedItems = metrics.asTimeMetrics()

        appliedEventsFilter?.let { which ->
            wrappedItems = interceptorEventsList[which].intercept(wrappedItems)
        }

        appliedThreadFilter?.let { which ->
            wrappedItems = threadsFilters[which].intercept(wrappedItems)
        }

        return wrappedItems.map { it.wrapped }
    }

    private fun showFilterDialog(interceptors: List<UiInterceptor>, onFilterSelected: (Int) -> Unit) {
        val names = interceptors.map { it.name }.toTypedArray()
        Builder(context)
            .setTitle(R.string.settings)
            .setItems(names) { _, which -> onFilterSelected(which) }
            .show()
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
                currentSortType = SortType.entries[which]
                binding.fastscroller.visibility = when (currentSortType) {
                    ALPHABET -> VISIBLE
                    TIME -> GONE
                }
                observeMetrics()
            }
            .create()
            .show()
    }

    private fun showExportFormatDialog() {
        val formats = arrayOf(
            context.getString(R.string.export_format_csv),
            context.getString(R.string.export_format_flamegraph),
            context.getString(R.string.export_format_firefox_profiler)
        )
        Builder(context)
            .setTitle(R.string.export_format_dialog_title)
            .setItems(formats) { _: DialogInterface?, which: Int ->
                exportJob?.cancel()
                exportJob = scope.launch(Dispatchers.IO) {
                    val rawMetrics = repository.getAllRawMetrics().asRawTraceMetrics()
                    when (which) {
                        0 -> {
                            shareRawCsv(context, rawMetrics, "tracer")
                        }

                        1 -> {
                            shareFlameGraph(context, rawMetrics, "tracer")
                        }

                        2 -> {
                            shareTrace(context, rawMetrics, "tracer")
                        }
                    }
                }
            }
            .create()
            .show()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        observeMetrics()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        metricsJob?.cancel()
        exportJob?.cancel()
    }

    private fun updateEmptyState() {
        if (pagingAdapter.itemCount == 0) {
            tvEmpty.visibility = VISIBLE
            lvMetrics.visibility = GONE
            tvEmpty.setText(R.string.no_collected_data)
        } else {
            tvEmpty.visibility = GONE
            lvMetrics.visibility = VISIBLE
        }
    }
}
