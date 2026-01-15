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

    /**
     * Get the full call stack for a specific execution by traversing parent chain.
     * Returns all frames from the given execution up to the root.
     */
    @Query("""
        WITH RECURSIVE call_stack AS (
            SELECT * FROM trace_metrics_raw WHERE executionId = :executionId
            UNION ALL
            SELECT r.* FROM trace_metrics_raw r
            INNER JOIN call_stack cs ON r.executionId = cs.parentExecutionId
        )
        SELECT * FROM call_stack ORDER BY depth ASC
    """)
    suspend fun getCallStackForExecution(executionId: Long): List<TraceMetricRawEntity>

    /**
     * Get all children (direct calls) made from a specific execution.
     */
    @Query("""
        SELECT * FROM trace_metrics_raw
        WHERE parentExecutionId = :executionId
        ORDER BY startTimeMs ASC
    """)
    suspend fun getChildCalls(executionId: Long): List<TraceMetricRawEntity>

    /**
     * Get the complete call tree starting from a root execution.
     * Returns all descendants of the given execution.
     */
    @Query("""
        WITH RECURSIVE call_tree AS (
            SELECT * FROM trace_metrics_raw WHERE executionId = :rootExecutionId
            UNION ALL
            SELECT r.* FROM trace_metrics_raw r
            INNER JOIN call_tree ct ON r.parentExecutionId = ct.executionId
        )
        SELECT * FROM call_tree ORDER BY depth ASC, startTimeMs ASC
    """)
    suspend fun getCallTree(rootExecutionId: Long): List<TraceMetricRawEntity>

    /**
     * Get all root calls (methods with no parent) within a time range.
     * Useful for finding entry points.
     */
    @Query("""
        SELECT * FROM trace_metrics_raw
        WHERE depth = 0 AND startTimeMs BETWEEN :startMs AND :endMs
        ORDER BY startTimeMs ASC
    """)
    suspend fun getRootCallsInTimeRange(startMs: Long, endMs: Long): List<TraceMetricRawEntity>

    /**
     * Get slow root calls (entry points with total time exceeding threshold).
     * These represent slow operations that can be expanded to see the full call tree.
     */
    @Query("""
        SELECT * FROM trace_metrics_raw
        WHERE depth = 0 AND durationMs > :thresholdMs
        ORDER BY durationMs DESC
    """)
    suspend fun getSlowRootCalls(thresholdMs: Long): List<TraceMetricRawEntity>

    /**
     * Get all calls within a time range with hierarchy information.
     * Ordered for flame graph visualization (by thread, then time, then depth).
     */
    @Query("""
        SELECT * FROM trace_metrics_raw
        WHERE startTimeMs BETWEEN :startMs AND :endMs
        ORDER BY threadName, startTimeMs, depth
    """)
    suspend fun getCallsInTimeRange(startMs: Long, endMs: Long): List<TraceMetricRawEntity>

    /**
     * Get raw trace for a specific execution by ID.
     */
    @Query("SELECT * FROM trace_metrics_raw WHERE executionId = :executionId LIMIT 1")
    suspend fun getTraceByExecutionId(executionId: Long): TraceMetricRawEntity?

    /**
     * Get all traces for a specific method, including call stack depth.
     * Useful for understanding how a method is called from different contexts.
     */
    @Query("""
        SELECT * FROM trace_metrics_raw
        WHERE methodId = :methodId
        ORDER BY startTimeMs DESC
    """)
    suspend fun getTracesForMethod(methodId: String): List<TraceMetricRawEntity>

    /**
     * Get aggregated stats per method including average depth.
     */
    @Query("""
        SELECT
            methodId,
            className,
            methodName,
            COUNT(*) as callCount,
            AVG(durationMs) as avgDurationMs,
            MAX(durationMs) as maxDurationMs,
            MIN(durationMs) as minDurationMs,
            AVG(depth) as avgDepth,
            MAX(depth) as maxDepth
        FROM trace_metrics_raw
        GROUP BY methodId
        ORDER BY MAX(durationMs) DESC
    """)
    suspend fun getMethodStats(): List<MethodStatsEntity>

    /**
     * Get all raw metrics ordered by thread, time, and depth for flame graph export.
     */
    @Query("""
        SELECT * FROM trace_metrics_raw
        ORDER BY threadName, startTimeMs, depth
    """)
    suspend fun getAllRawMetrics(): List<TraceMetricRawEntity>
}
