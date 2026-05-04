package com.yandex.demeter.profiler.coroutine.tracer.internal.data

import android.content.Context
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.CoroutineMetricDao
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.CoroutineMetricRawEntity
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.db.CoroutineTracerDatabase
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.AsmCoroutineMetric
import com.yandex.demeter.profiler.coroutine.tracer.internal.data.model.CoroutineTraceNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@InternalDemeterApi
interface CoroutineMetricsRepository {
    fun getCoroutineTreeRoots(): Flow<List<CoroutineTraceNode>>
    suspend fun getRawMetrics(): List<CoroutineMetricRawEntity>
    suspend fun getCoroutineTree(rootTraceId: Long): CoroutineTraceNode?
    suspend fun getSlowCoroutines(thresholdMs: Long): List<CoroutineMetricRawEntity>
    suspend fun getCancelledCoroutines(): List<CoroutineMetricRawEntity>
    suspend fun clear()
    suspend fun upsertMetric(metric: AsmCoroutineMetric)
}

@InternalDemeterApi
class CoroutineMetricsRepositoryImpl private constructor(
    private val dao: CoroutineMetricDao,
) : CoroutineMetricsRepository {

    override fun getCoroutineTreeRoots(): Flow<List<CoroutineTraceNode>> {
        return dao.getAllFlow().map { entities ->
            if (entities.isEmpty()) return@map emptyList()
            val roots = entities.filter { it.parentTraceId == null || it.depth == 0 }
            roots.mapNotNull { root -> buildTree(root.traceId, entities) }
        }
    }

    override suspend fun getRawMetrics(): List<CoroutineMetricRawEntity> {
        return dao.getAll()
    }

    override suspend fun getCoroutineTree(rootTraceId: Long): CoroutineTraceNode? {
        val allNodes = dao.getCoroutineTree(rootTraceId)
        return buildTree(rootTraceId, allNodes)
    }

    override suspend fun getSlowCoroutines(thresholdMs: Long): List<CoroutineMetricRawEntity> {
        return dao.getSlowCoroutines(thresholdMs)
    }

    override suspend fun getCancelledCoroutines(): List<CoroutineMetricRawEntity> {
        return dao.getCancelledCoroutines()
    }

    override suspend fun clear() {
        dao.clear()
    }

    override suspend fun upsertMetric(metric: AsmCoroutineMetric) {
        dao.insertMetric(
            CoroutineMetricRawEntity(
                traceId = metric.traceId,
                parentTraceId = metric.parentTraceId,
                launchSite = metric.launchSite,
                durationMs = metric.durationMs,
                startTimeMs = metric.startTimeMs,
                launchThreadName = metric.launchThreadName,
                completionThreadName = metric.completionThreadName,
                isCancelled = metric.isCancelled,
                exception = metric.exception,
                depth = metric.depth,
                dispatcherName = metric.dispatcherName,
            )
        )
    }

    private fun buildTree(
        rootTraceId: Long,
        entities: List<CoroutineMetricRawEntity>,
    ): CoroutineTraceNode? {
        if (entities.isEmpty()) return null
        val rootEntity = entities.firstOrNull { it.traceId == rootTraceId } ?: return null
        val childrenMap = entities.groupBy { it.parentTraceId }
        // Build leaves first so parents can reference already-constructed children.
        val built = HashMap<Long, CoroutineTraceNode>(entities.size)
        entities.sortedByDescending { it.depth }.forEach { entity ->
            val childNodes = childrenMap[entity.traceId]
                ?.mapNotNull { built[it.traceId] }
                ?: emptyList()
            built[entity.traceId] = entity.toTraceNode(childNodes)
        }
        return built[rootEntity.traceId]
    }

    companion object {
        @Volatile
        private var instance: CoroutineMetricsRepositoryImpl? = null

        fun getInstance(context: Context): CoroutineMetricsRepository =
            instance ?: synchronized(this) {
                instance ?: CoroutineMetricsRepositoryImpl(
                    CoroutineTracerDatabase.getInstance(context).coroutineMetricDao()
                ).also { instance = it }
            }
    }
}

private fun CoroutineMetricRawEntity.toTraceNode(
    children: List<CoroutineTraceNode> = emptyList(),
): CoroutineTraceNode {
    val simpleName = launchSite.substringAfterLast('.').ifEmpty { launchSite }
    return CoroutineTraceNode(
        traceId = traceId,
        parentTraceId = parentTraceId,
        launchSite = launchSite,
        simpleName = simpleName,
        durationMs = durationMs,
        startTimeMs = startTimeMs,
        launchThreadName = launchThreadName,
        completionThreadName = completionThreadName,
        isCancelled = isCancelled,
        exception = exception,
        depth = depth,
        dispatcherName = dispatcherName,
        children = children,
    )
}
