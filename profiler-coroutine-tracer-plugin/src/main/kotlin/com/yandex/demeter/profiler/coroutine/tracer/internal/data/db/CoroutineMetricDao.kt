package com.yandex.demeter.profiler.coroutine.tracer.internal.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yandex.demeter.annotations.InternalDemeterApi
import kotlinx.coroutines.flow.Flow

@InternalDemeterApi
@Dao
interface CoroutineMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(entity: CoroutineMetricRawEntity)

    @Query("SELECT * FROM coroutine_metrics_raw ORDER BY startTimeMs DESC")
    suspend fun getAll(): List<CoroutineMetricRawEntity>

    @Query("SELECT * FROM coroutine_metrics_raw ORDER BY startTimeMs DESC")
    fun getAllFlow(): Flow<List<CoroutineMetricRawEntity>>

    @Query("SELECT * FROM coroutine_metrics_raw WHERE parentTraceId = :parentTraceId ORDER BY startTimeMs ASC")
    suspend fun getChildrenOf(parentTraceId: Long): List<CoroutineMetricRawEntity>

    /**
     * Get the complete coroutine tree starting from a root trace.
     * Returns all descendants of the given coroutine.
     */
    @Query("""
        WITH RECURSIVE coroutine_tree AS (
            SELECT * FROM coroutine_metrics_raw WHERE traceId = :rootTraceId
            UNION ALL
            SELECT r.* FROM coroutine_metrics_raw r
            INNER JOIN coroutine_tree ct ON r.parentTraceId = ct.traceId
        )
        SELECT * FROM coroutine_tree ORDER BY depth ASC, startTimeMs ASC
    """)
    suspend fun getCoroutineTree(rootTraceId: Long): List<CoroutineMetricRawEntity>

    @Query("SELECT * FROM coroutine_metrics_raw WHERE depth = 0 AND durationMs > :thresholdMs ORDER BY durationMs DESC")
    suspend fun getSlowCoroutines(thresholdMs: Long): List<CoroutineMetricRawEntity>

    @Query("SELECT * FROM coroutine_metrics_raw WHERE isCancelled = 1 ORDER BY startTimeMs DESC")
    suspend fun getCancelledCoroutines(): List<CoroutineMetricRawEntity>

    @Query("DELETE FROM coroutine_metrics_raw")
    suspend fun clear(): Int
}
