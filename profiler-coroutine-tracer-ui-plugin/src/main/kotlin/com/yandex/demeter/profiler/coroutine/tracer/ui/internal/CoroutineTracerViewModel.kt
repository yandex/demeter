package com.yandex.demeter.profiler.coroutine.tracer.ui.internal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.CoroutineTraceNode

/**
 * Wrapper for a [CoroutineTraceNode] in the flattened visible list,
 * carrying tree-drawing metadata.
 *
 * @param node The original trace node.
 * @param isLastChild Whether this node is the last sibling at its depth level.
 * @param activeConnectors Set of depth levels (0-based) where a vertical "│" line
 *   should be drawn because an ancestor at that level still has siblings below.
 */
@InternalDemeterApi
internal data class VisibleNode(
    val node: CoroutineTraceNode,
    val isLastChild: Boolean,
    val activeConnectors: Set<Int>,
    val parentSimpleName: String? = null,
)

@InternalDemeterApi
internal class CoroutineTracerViewModel {

    private val _sortType = mutableStateOf(SortType.TIME)
    val sortType: State<SortType> = _sortType

    private val _threadFilter = mutableStateOf<String?>(null)
    val threadFilter: State<String?> = _threadFilter

    private val _expandedNodes = mutableStateMapOf<Long, Boolean>()

    fun isExpanded(traceId: Long): Boolean = _expandedNodes[traceId] == true

    fun toggleExpanded(traceId: Long) {
        _expandedNodes[traceId] = !isExpanded(traceId)
    }

    fun expandAll(nodes: List<CoroutineTraceNode>) {
        // Iterative BFS to avoid StackOverflow on deep coroutine chains.
        val stack = ArrayDeque<CoroutineTraceNode>()
        stack.addAll(nodes)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            if (node.children.isNotEmpty()) {
                _expandedNodes[node.traceId] = true
                stack.addAll(node.children)
            }
        }
    }

    fun collapseAll() {
        _expandedNodes.clear()
    }

    fun setSortType(sort: SortType) {
        _sortType.value = sort
    }

    fun setThreadFilter(thread: String?) {
        _threadFilter.value = thread
    }

    /**
     * Flattens the tree into a list of [VisibleNode]s based on the current expand/collapse state.
     * Each entry carries tree-connector metadata ([VisibleNode.isLastChild], [VisibleNode.activeConnectors])
     * so the UI can render `├──` / `└──` / `│` prefixes.
     */
    fun flattenVisibleNodes(roots: List<CoroutineTraceNode>): List<VisibleNode> {
        // Iterative pre-order DFS to avoid StackOverflow on deep coroutine chains.
        val result = mutableListOf<VisibleNode>()
        val stack = ArrayDeque<Frame>()
        for (i in roots.indices.reversed()) {
            stack.addLast(
                Frame(
                    node = roots[i],
                    isLast = i == roots.lastIndex,
                    connectors = emptySet(),
                    parentSimpleName = null,
                )
            )
        }
        while (stack.isNotEmpty()) {
            val frame = stack.removeLast()
            result.add(
                VisibleNode(
                    node = frame.node,
                    isLastChild = frame.isLast,
                    activeConnectors = frame.connectors,
                    parentSimpleName = frame.parentSimpleName,
                )
            )
            if (isExpanded(frame.node.traceId)) {
                val children = frame.node.children
                // Push children in reverse so the first child is popped (visited) next.
                for (i in children.indices.reversed()) {
                    val child = children[i]
                    val childIsLast = i == children.lastIndex
                    val childConnectors = if (childIsLast) {
                        frame.connectors
                    } else {
                        frame.connectors + child.depth
                    }
                    stack.addLast(
                        Frame(
                            node = child,
                            isLast = childIsLast,
                            connectors = childConnectors,
                            parentSimpleName = frame.node.simpleName,
                        )
                    )
                }
            }
        }
        return result
    }

    private data class Frame(
        val node: CoroutineTraceNode,
        val isLast: Boolean,
        val connectors: Set<Int>,
        val parentSimpleName: String?,
    )

    enum class SortType {
        TIME,
        DURATION,
        NAME,
    }
}
