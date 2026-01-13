package com.yandex.demeter.profiler.tracer.internal.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yandex.demeter.annotations.InternalDemeterApi
import kotlinx.coroutines.flow.Flow

private const val AGGREGATION_QUERY = """
    SELECT
        methodId AS id,
        className,
        methodName,
        COUNT(*) AS count,
        MAX(durationMs) AS maxDurationMs,
        (SELECT r2.durationMs FROM trace_metrics_raw r2 WHERE r2.methodId = r.methodId ORDER BY r2.startTimeMs DESC LIMIT 1) AS lastDurationMs,
        MAX(startTimeMs) AS lastStartTimeMs,
        (SELECT r2.threadName FROM trace_metrics_raw r2 WHERE r2.methodId = r.methodId ORDER BY r2.startTimeMs DESC LIMIT 1) AS lastThreadName,
        MAX(createdAt) AS updatedAt
    FROM trace_metrics_raw r
    GROUP BY methodId
"""

@InternalDemeterApi
@Dao
interface TraceMetricDao {

    @Query("$AGGREGATION_QUERY ORDER BY maxDurationMs DESC")
    fun getAllByTimeDescPaged(): PagingSource<Int, TraceMetricEntity>

    @Query("$AGGREGATION_QUERY ORDER BY className ASC, methodName ASC")
    fun getAllByNameAscPaged(): PagingSource<Int, TraceMetricEntity>

    @Query("$AGGREGATION_QUERY ORDER BY maxDurationMs DESC")
    fun getAllByTimeDescFlow(): Flow<List<TraceMetricEntity>>

    @Query("$AGGREGATION_QUERY ORDER BY className ASC, methodName ASC")
    fun getAllByNameAscFlow(): Flow<List<TraceMetricEntity>>

    @Insert
    suspend fun insertMetric(entity: TraceMetricRawEntity)

    @Query("DELETE FROM trace_metrics_raw")
    suspend fun clear(): Int
}
