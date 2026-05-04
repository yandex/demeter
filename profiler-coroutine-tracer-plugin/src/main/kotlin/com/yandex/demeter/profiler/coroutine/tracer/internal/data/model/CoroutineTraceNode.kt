package com.yandex.demeter.profiler.coroutine.tracer.internal.data.model

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
data class CoroutineTraceNode(
    val traceId: Long,
    val parentTraceId: Long?,
    val launchSite: String,
    val simpleName: String,
    val durationMs: Long,
    val startTimeMs: Long,
    val launchThreadName: String,
    val completionThreadName: String,
    val isCancelled: Boolean,
    val exception: String?,
    val depth: Int,
    val dispatcherName: String? = null,
    val children: List<CoroutineTraceNode> = emptyList(),
)
