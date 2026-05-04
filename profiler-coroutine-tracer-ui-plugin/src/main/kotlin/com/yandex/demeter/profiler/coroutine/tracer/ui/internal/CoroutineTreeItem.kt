package com.yandex.demeter.profiler.coroutine.tracer.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.CoroutineTraceNode
import com.yandex.demeter.profiler.coroutine.tracer.ui.internal.theme.CoroutineTracerColors

/**
 * A single row in the coroutine trace tree.
 *
 * Shows:
 * - Tree connector lines (├──, └──, │) for visual hierarchy
 * - Expand/collapse icon if the node has children
 * - Simple name of the launch site
 * - Duration badge (color-coded by [WarningLevel])
 * - Thread name chip
 * - Status indicator (cancelled / error)
 */
@OptIn(InternalDemeterApi::class)
@Composable
internal fun CoroutineTreeItem(
    visibleNode: VisibleNode,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val node = visibleNode.node
    val warningLevel = WarningLevel.getLevel(node.durationMs)
    val backgroundColor = CoroutineTracerColors.backgroundForLevel(warningLevel)
    val textColor = CoroutineTracerColors.textForLevel(warningLevel)
    val descriptionColor = CoroutineTracerColors.descriptionTextForLevel(warningLevel)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(enabled = node.children.isNotEmpty()) { onToggleExpand() }
            .padding(
                start = 8.dp,
                top = 6.dp,
                bottom = 6.dp,
                end = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tree connector prefix
        if (node.depth > 0) {
            Text(
                text = buildTreePrefix(node.depth, visibleNode.isLastChild, visibleNode.activeConnectors),
                color = descriptionColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Expand/collapse indicator or spacer
        if (node.children.isNotEmpty()) {
            Text(
                text = if (isExpanded) "\u25BC" else "\u25B6",
                color = textColor,
                fontSize = 12.sp,
                modifier = Modifier.width(20.dp),
            )
        } else {
            Spacer(modifier = Modifier.width(20.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Node content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortenedName(node.simpleName, visibleNode.parentSimpleName),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Duration
                Text(
                    text = "${node.durationMs}ms",
                    fontSize = 12.sp,
                    color = descriptionColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Dispatcher chip (if available) or thread chips
                val dispatcher = node.dispatcherName
                if (dispatcher != null) {
                    ThreadChip(threadName = dispatcher)
                } else {
                    ThreadChip(threadName = node.launchThreadName)
                    if (node.completionThreadName != node.launchThreadName) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "\u2192", fontSize = 10.sp, color = descriptionColor)
                        Spacer(modifier = Modifier.width(2.dp))
                        ThreadChip(threadName = node.completionThreadName)
                    }
                }
                // Cancelled badge
                if (node.isCancelled) {
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusBadge(
                        text = "CANCELLED",
                        color = CoroutineTracerColors.cancelledBadge,
                    )
                }
                // Error badge
                if (node.exception != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusBadge(
                        text = "ERROR",
                        color = CoroutineTracerColors.errorBadge,
                    )
                }
            }
        }
    }
}

/**
 * Shortens [name] by removing the common class prefix shared with [parentName].
 *
 * The parent's full class path (everything before `#`) is used as the prefix.
 * This means each level only shows what's new relative to its immediate parent.
 *
 * Examples:
 * - parent: `CoroutineShowCase#retryPattern:220`
 *   child:  `CoroutineShowCase$retryPattern$1#invokeSuspend:221`
 *   result: `…$retryPattern$1#invokeSuspend:221`
 *
 * - parent: `CoroutineShowCase$retryPattern$1#invokeSuspend:221`
 *   child:  `CoroutineShowCase$retryPattern$1$1#invokeSuspend:224`
 *   result: `…$1#invokeSuspend:224`
 *
 * - parent: `Foo#bar:10`, child: `Baz#qux:20` → `Baz#qux:20` (no common prefix)
 */
private fun shortenedName(name: String, parentName: String?): String {
    if (parentName == null) return name

    // Extract the full class path (before '#') from the parent name
    val hashIndex = parentName.indexOf('#')
    val parentClass = if (hashIndex >= 0) parentName.substring(0, hashIndex) else parentName
    if (parentClass.isEmpty()) return name

    if (!name.startsWith(parentClass)) return name

    return "\u2026" + name.removePrefix(parentClass) // "…" + remaining part
}

/**
 * Builds a tree-view prefix string for a node, e.g. `│  ├─ ` or `   └─ `.
 */
private fun buildTreePrefix(
    depth: Int,
    isLastChild: Boolean,
    activeConnectors: Set<Int>,
): String = buildString {
    // Ancestor continuation lines
    for (level in 1 until depth) {
        if (level in activeConnectors) {
            append("\u2502  ") // "│  "
        } else {
            append("   ")
        }
    }
    // Branch connector for this node
    if (isLastChild) {
        append("\u2514\u2500 ") // "└─ "
    } else {
        append("\u251C\u2500 ") // "├─ "
    }
}
