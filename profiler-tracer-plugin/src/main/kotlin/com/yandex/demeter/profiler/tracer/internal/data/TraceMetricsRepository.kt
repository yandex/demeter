package com.yandex.demeter.profiler.tracer.internal.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.utils.SortType
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
                threadName = asmMetric.threadName
            )
        )
    }

    override suspend fun clear() {
        dao.clear()
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
