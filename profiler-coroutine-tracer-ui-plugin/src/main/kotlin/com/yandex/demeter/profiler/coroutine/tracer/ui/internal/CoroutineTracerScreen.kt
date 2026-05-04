package com.yandex.demeter.profiler.coroutine.tracer.ui.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.utils.shareFlameGraph
import com.yandex.demeter.internal.utils.shareRawCsv
import com.yandex.demeter.internal.utils.shareTrace
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.CoroutineMetricsRepositoryImpl
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.asRawTraceMetrics
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.CoroutineTraceNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main screen composable for the Coroutine Tracer UI plugin.
 *
 * Displays a tree of [CoroutineTraceNode] items with expand/collapse,
 * sorting, and thread filtering capabilities.
 */
@OptIn(InternalDemeterApi::class)
@Composable
internal fun CoroutineTracerScreen() {
    val context = LocalContext.current
    val viewModel = remember { CoroutineTracerViewModel() }
    val repository = remember { CoroutineMetricsRepositoryImpl.getInstance(context) }
    val scope = rememberCoroutineScope()

    val sortType by viewModel.sortType
    val threadFilter by viewModel.threadFilter
    var showExportDialog by remember { mutableStateOf(false) }

    val rootNodes by repository.getCoroutineTreeRoots().collectAsState(initial = emptyList())

    val sortedRoots = remember(rootNodes, sortType) {
        when (sortType) {
            CoroutineTracerViewModel.SortType.TIME -> rootNodes.sortedByDescending { it.startTimeMs }
            CoroutineTracerViewModel.SortType.DURATION -> rootNodes.sortedByDescending { it.durationMs }
            CoroutineTracerViewModel.SortType.NAME -> rootNodes.sortedBy { it.simpleName }
        }
    }

    val filteredRoots = remember(sortedRoots, threadFilter) {
        if (threadFilter == null) {
            sortedRoots
        } else {
            sortedRoots.filter { it.launchThreadName == threadFilter }
        }
    }

    val visibleNodes by remember(filteredRoots) {
        derivedStateOf { viewModel.flattenVisibleNodes(filteredRoots) }
    }

    // Collect unique thread names for the filter menu
    val threadNames = remember(rootNodes) {
        rootNodes.map { it.launchThreadName }.distinct().sorted()
    }

    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onFormatSelected = { format ->
                showExportDialog = false
                scope.launch(Dispatchers.IO) {
                    val rawMetrics = repository.getRawMetrics().asRawTraceMetrics()
                    when (format) {
                        ExportFormat.CSV -> shareRawCsv(context, rawMetrics, "coroutine-tracer")
                        ExportFormat.FLAMEGRAPH -> shareFlameGraph(context, rawMetrics, "coroutine-tracer")
                        ExportFormat.FIREFOX_PROFILER -> shareTrace(context, rawMetrics, "coroutine-tracer")
                    }
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CoroutineTracerTopBar(
            sortType = sortType,
            onSortChanged = { viewModel.setSortType(it) },
            threadFilter = threadFilter,
            onThreadFilterChanged = { viewModel.setThreadFilter(it) },
            threadNames = threadNames,
            onExpandAll = { viewModel.expandAll(filteredRoots) },
            onCollapseAll = { viewModel.collapseAll() },
            onExport = { showExportDialog = true },
        )

        Divider()

        if (filteredRoots.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = visibleNodes,
                    key = { it.node.traceId },
                ) { visibleNode ->
                    CoroutineTreeItem(
                        visibleNode = visibleNode,
                        isExpanded = viewModel.isExpanded(visibleNode.node.traceId),
                        onToggleExpand = { viewModel.toggleExpanded(visibleNode.node.traceId) },
                    )
                    Divider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
internal fun CoroutineTracerTopBar(
    sortType: CoroutineTracerViewModel.SortType,
    onSortChanged: (CoroutineTracerViewModel.SortType) -> Unit,
    threadFilter: String?,
    onThreadFilterChanged: (String?) -> Unit,
    threadNames: List<String>,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showThreadMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Coroutine Tracer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )

        // Expand all
        Text(
            text = "\u2725",
            modifier = Modifier
                .clickable { onExpandAll() }
                .padding(8.dp),
        )

        // Collapse all
        Text(
            text = "\u2296",
            modifier = Modifier
                .clickable { onCollapseAll() }
                .padding(8.dp),
        )

        // Export
        Text(
            text = "\u21E7",
            modifier = Modifier
                .clickable { onExport() }
                .padding(8.dp),
        )

        // Sort button
        Box {
            Text(
                text = "\u2195",
                modifier = Modifier
                    .clickable { showSortMenu = true }
                    .padding(8.dp),
            )
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                CoroutineTracerViewModel.SortType.entries.forEach { type ->
                    DropdownMenuItem(
                        onClick = {
                            onSortChanged(type)
                            showSortMenu = false
                        },
                    ) {
                        Text(
                            text = type.name,
                            fontWeight = if (type == sortType) {
                                FontWeight.Bold
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }

        // Thread filter button
        Box {
            Text(
                text = "\u2630",
                modifier = Modifier
                    .clickable { showThreadMenu = true }
                    .padding(8.dp),
            )
            DropdownMenu(
                expanded = showThreadMenu,
                onDismissRequest = { showThreadMenu = false },
            ) {
                DropdownMenuItem(
                    onClick = {
                        onThreadFilterChanged(null)
                        showThreadMenu = false
                    },
                ) {
                    Text(
                        text = "All threads",
                        fontWeight = if (threadFilter == null) {
                            FontWeight.Bold
                        } else {
                            null
                        },
                    )
                }
                threadNames.forEach { thread ->
                    DropdownMenuItem(
                        onClick = {
                            onThreadFilterChanged(thread)
                            showThreadMenu = false
                        },
                    ) {
                        Text(
                            text = thread,
                            fontWeight = if (thread == threadFilter) {
                                FontWeight.Bold
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}

private enum class ExportFormat(val label: String) {
    CSV("CSV"),
    FLAMEGRAPH("Flamegraph"),
    FIREFOX_PROFILER("Firefox Profiler"),
}

@Composable
private fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export format") },
        text = {
            Column {
                ExportFormat.entries.forEach { format ->
                    Text(
                        text = format.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFormatSelected(format) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
