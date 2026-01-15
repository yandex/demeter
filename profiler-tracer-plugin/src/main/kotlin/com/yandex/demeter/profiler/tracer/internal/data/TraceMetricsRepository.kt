package com.yandex.demeter.profiler.tracer.internal.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.utils.SortType
import com.yandex.demeter.profiler.tracer.internal.data.db.MethodStatsEntity
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricDao
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricEntity
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricRawEntity
import com.yandex.demeter.profiler.tracer.internal.data.db.TracerDatabase
import com.yandex.demeter.profiler.tracer.internal.data.model.AsmTraceMetric
import kotlinx.coroutines.flow.Flow

private const val PAGE_SIZE = 30

@InternalDemeterApi
interface TraceMetricsRepository {
    fun getMetricsFlow(sortType: SortType): Flow<List<TraceMetricEntity>>
    fun getMetricsPaged(sortType: SortType): Flow<PagingData<TraceMetricEntity>>
    suspend fun upsertMetric(asmMetric: AsmTraceMetric)
    suspend fun clear()

    suspend fun getCallStackForExecution(executionId: Long): List<TraceMetricRawEntity>
    suspend fun getCallTree(rootExecutionId: Long): List<TraceMetricRawEntity>
    suspend fun getChildCalls(executionId: Long): List<TraceMetricRawEntity>
    suspend fun getSlowRootCalls(thresholdMs: Long): List<TraceMetricRawEntity>
    suspend fun getCallsInTimeRange(startMs: Long, endMs: Long): List<TraceMetricRawEntity>
    suspend fun getMethodStats(): List<MethodStatsEntity>
    suspend fun getAllRawMetrics(): List<TraceMetricRawEntity>
}

@InternalDemeterApi
class TraceMetricsRepositoryImpl private constructor(
    private val dao: TraceMetricDao
) : TraceMetricsRepository {

    override fun getMetricsFlow(sortType: SortType): Flow<List<TraceMetricEntity>> {
        return when (sortType) {
            SortType.TIME -> dao.getAllByTimeDescFlow()
            SortType.ALPHABET -> dao.getAllByNameAscFlow()
        }
    }

    override fun getMetricsPaged(sortType: SortType): Flow<PagingData<TraceMetricEntity>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                when (sortType) {
                    SortType.TIME -> dao.getAllByTimeDescPaged()
                    SortType.ALPHABET -> dao.getAllByNameAscPaged()
                }
            }
        ).flow
    }

    override suspend fun upsertMetric(asmMetric: AsmTraceMetric) {
        dao.insertMetric(
            TraceMetricRawEntity(
                methodId = asmMetric.id,
                className = asmMetric.className,
                methodName = asmMetric.methodName,
                durationMs = asmMetric.durationMs,
                startTimeMs = asmMetric.startTimeMs,
                threadName = asmMetric.threadName,
                executionId = asmMetric.executionId,
                parentExecutionId = asmMetric.parentExecutionId,
                parentMethodId = asmMetric.parentMethodId,
                depth = asmMetric.depth,
            )
        )
    }

    override suspend fun clear() {
        dao.clear()
    }

    override suspend fun getCallStackForExecution(executionId: Long): List<TraceMetricRawEntity> {
        return dao.getCallStackForExecution(executionId)
    }

    override suspend fun getCallTree(rootExecutionId: Long): List<TraceMetricRawEntity> {
        return dao.getCallTree(rootExecutionId)
    }

    override suspend fun getChildCalls(executionId: Long): List<TraceMetricRawEntity> {
        return dao.getChildCalls(executionId)
    }

    override suspend fun getSlowRootCalls(thresholdMs: Long): List<TraceMetricRawEntity> {
        return dao.getSlowRootCalls(thresholdMs)
    }

    override suspend fun getCallsInTimeRange(startMs: Long, endMs: Long): List<TraceMetricRawEntity> {
        return dao.getCallsInTimeRange(startMs, endMs)
    }

    override suspend fun getMethodStats(): List<MethodStatsEntity> {
        return dao.getMethodStats()
    }

    override suspend fun getAllRawMetrics(): List<TraceMetricRawEntity> {
        return dao.getAllRawMetrics()
    }

    companion object {
        @Volatile
        private var instance: TraceMetricsRepositoryImpl? = null

        fun getInstance(context: Context): TraceMetricsRepository =
            instance ?: synchronized(this) {
                instance ?: TraceMetricsRepositoryImpl(
                    TracerDatabase.getInstance(context).traceMetricDao()
                ).also { instance = it }
            }
    }
}
