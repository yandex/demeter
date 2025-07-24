package com.yandex.demeter.profiler.compose.internal.ir.tracer

import android.util.Log
import com.yandex.demeter.profiler.compose.internal.core.TAG

internal object ComposeTracer {
    private val stack = ArrayDeque<String>()
    private var rootTreeNode: ComposeTreeNode? = null
    private var currentTreeNode: ComposeTreeNode? = null

    val latestCallStack: ComposeTreeNode?
        get() = rootTreeNode?.copy()

    @JvmStatic
    fun push(name: String) {
        if (stack.isEmpty()) {
            rootTreeNode = null
            currentTreeNode = null
        }

        stack.addLast(name)
        if (currentTreeNode == null) {
            rootTreeNode = ComposeTreeNode(name, null)
            currentTreeNode = rootTreeNode
        } else {
            val newNode = ComposeTreeNode(name, currentTreeNode)
            currentTreeNode?.children?.add(newNode)
            currentTreeNode = newNode
        }
    }

    @JvmStatic
    fun pop(name: String) {
        stack.removeLast()

        currentTreeNode = currentTreeNode?.parent

        if (stack.isEmpty()) {
            rootTreeNode?.logTree()
        }
    }
}

internal data class ComposeTreeNode(
    val className: String,
    val parent: ComposeTreeNode?,
    val children: MutableList<ComposeTreeNode> = mutableListOf()
)

internal fun ComposeTreeNode.logTree() {
    logTree(depth = 1)
}

private fun ComposeTreeNode.logTree(depth: Int) {
    Log.d(TAG, "-".repeat(depth) + " " + className)
    children.forEach {
        it.logTree(depth + 1)
    }
}
