package com.yandex.demeter.internal.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.getUriForFile
import com.yandex.demeter.internal.model.TimeMetric
import com.yandex.demeter.internal.model.RawTraceMetric
import java.io.BufferedWriter
import java.io.File
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CURRENT_TIME_FORMATTER = SimpleDateFormat("dd-MM-yyyy_kk-mm-ss", Locale.US)

private const val CSV_COLUMNS_DELIMITER = ','
private const val INJECT_PATH_SEPARATOR = ';'
private const val FLAMEGRAPH_STACK_SEPARATOR = ';'

@InternalDemeterApi
fun shareCsv(context: Context, metrics: Collection<TimeMetric>, logName: String) {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val sharedFile = checkFileForPathTraversal(
        File(directory, "${logName}-${getCurrentDateTime()}.csv"),
        directory
    )

    directory.mkdirs()

    sharedFile.bufferedWriter().use {
        // header
        it.append("depth").appendDelimiter()
        it.append("init time (ms)").appendDelimiter()
        it.append("warning level").appendDelimiter()
        it.append("thread").appendDelimiter()
        it.append("class#method").appendLine()

        // values
        metrics.forEach { metric ->
            it.append("0").appendDelimiter() // root
            it.append(metric.totalInitTime.toString()).appendDelimiter()
            it.append(WarningLevel.getLevel(metric.totalInitTime).toString()).appendDelimiter()
            it.append(metric.threadName).appendDelimiter()
            it.append(metric.simpleName).appendLine()

            it.appendInjectArgs(arrayOf(metric.simpleName), metric.args)
        }
    }

    val intent = Intent()
        .setAction(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_STREAM, context.getUriForFile(sharedFile))
        .setType("text/csv")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(Intent.createChooser(intent, null))
}

private fun BufferedWriter.appendInjectArgs(
    path: Array<String>,
    args: List<TimeMetric>,
    depth: Int = 1,
) {
    args.forEach { metric ->
        append(depth.toString()).appendDelimiter()
        append(metric.totalInitTime.toString()).appendDelimiter()
        append(WarningLevel.getLevel(metric.totalInitTime).toString()).appendDelimiter()
        append(metric.threadName).appendDelimiter()
        path.forEach { pathPart ->
            append(pathPart).appendInjectPathSeparator()
        }
        append(metric.simpleName).appendLine()
        appendInjectArgs(path + arrayOf(metric.simpleName), metric.args, depth + 1)
    }
}

private fun getCurrentDateTime(): String {
    return CURRENT_TIME_FORMATTER.format(Date())
}

@Throws(IllegalArgumentException::class)
private fun checkFileForPathTraversal(file: File, expectedDir: File?): File {
    val canonicalPath = file.canonicalPath
    require(canonicalPath.startsWith(expectedDir!!.canonicalPath))
    return file
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Writer.appendDelimiter() {
    append(CSV_COLUMNS_DELIMITER)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Writer.appendInjectPathSeparator() {
    append(INJECT_PATH_SEPARATOR)
}

/**
 * Exports raw trace metrics in CSV format compatible with convert.py script.
 * Format: start,end,depth,name,thread
 */
@InternalDemeterApi
fun shareRawCsv(context: Context, metrics: Collection<RawTraceMetric>, logName: String) {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val sharedFile = checkFileForPathTraversal(
        File(directory, "${logName}-raw-${getCurrentDateTime()}.csv"),
        directory
    )

    directory.mkdirs()

    sharedFile.bufferedWriter().use { writer ->
        // Sort by start time for proper visualization
        val sortedMetrics = metrics.sortedBy { it.startTimeMs }

        for (metric in sortedMetrics) {
            val endTimeMs = metric.startTimeMs + metric.durationMs
            // Format: start,end,depth,name,thread
            writer.append(metric.startTimeMs.toString()).appendDelimiter()
            writer.append(endTimeMs.toString()).appendDelimiter()
            writer.append(metric.depth.toString()).appendDelimiter()
            writer.append(metric.simpleName).appendDelimiter()
            writer.append(metric.threadName).appendLine()
        }
    }

    val intent = Intent()
        .setAction(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_STREAM, context.getUriForFile(sharedFile))
        .setType("text/csv")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(Intent.createChooser(intent, null))
}

/**
 * Exports as a flamegraph format.
 *
 * Usage: FlameGraph/flamegraph.pl --flamechart  tracer-flamegraph.txt > tracer.svg
 */
@InternalDemeterApi
fun shareFlameGraph(context: Context, metrics: Collection<RawTraceMetric>, logName: String) {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val sharedFile = checkFileForPathTraversal(
        File(directory, "${logName}-flamegraph-${getCurrentDateTime()}.txt"),
        directory
    )

    directory.mkdirs()

    val executionIdToMetric = metrics.associateBy { it.executionId }

    sharedFile.bufferedWriter().use { writer ->
        val byThread = metrics.groupBy { it.threadName }

        for ((threadName, threadMetrics) in byThread) {
            for (metric in threadMetrics) {
                val stack = buildStackTrace(metric, executionIdToMetric)
                if (stack.isNotEmpty()) {
                    // Format: thread;frame1;frame2;...;frameN duration
                    writer.append(threadName)
                    for (frame in stack) {
                        writer.append(FLAMEGRAPH_STACK_SEPARATOR)
                        writer.append(frame)
                    }
                    writer.append(' ')
                    writer.append(metric.durationMs.toString())
                    writer.appendLine()
                }
            }
        }
    }

    val intent = Intent()
        .setAction(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_STREAM, context.getUriForFile(sharedFile))
        .setType("text/plain")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(Intent.createChooser(intent, null))
}

private fun buildStackTrace(
    metric: RawTraceMetric,
    executionIdToMetric: Map<Long, RawTraceMetric>
): List<String> {
    val stack = mutableListOf<String>()

    var current: RawTraceMetric? = metric
    while (current != null) {
        stack.add(0, current.simpleName)
        current = current.parentExecutionId?.let { executionIdToMetric[it] }
    }

    return stack
}
/**
 * Exports as a Firefox profiler format to use in https://profiler.firefox.com.
 */
@InternalDemeterApi
fun shareTrace(context: Context, metrics: Collection<RawTraceMetric>, logName: String) {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val sharedFile = checkFileForPathTraversal(
        File(directory, "${logName}-trace-${getCurrentDateTime()}.json"),
        directory
    )

    directory.mkdirs()

    sharedFile.bufferedWriter().use { writer ->
        val pid = android.os.Process.myPid()
        val sortedMetrics = metrics.sortedBy { it.startTimeMs }
        val baseTimeMs = sortedMetrics.minOfOrNull { it.startTimeMs } ?: 0L

        val stringArray = mutableListOf<String>()
        val stringToIndex = mutableMapOf<String, Int>()

        fun String.intern(): Int = stringToIndex.getOrPut(this) {
            stringArray.add(this)
            stringArray.lastIndex
        }

        fun filledArray(size: Int, value: String) = (0 until size).joinToString(",", "[", "]") { value }

        val threadsJson = sortedMetrics.groupBy { it.threadName }.entries.joinToString(",") { (threadName, threadMetrics) ->
            val tid = threadName.hashCode().and(0x7FFFFFFF)

            val funcNames = mutableListOf<Int>()
            val frameFunc = mutableListOf<Int>()
            val stackFrame = mutableListOf<Int>()
            val stackPrefix = mutableListOf<Int?>()
            val sampleStack = mutableListOf<Int>()
            val sampleTime = mutableListOf<Double>()
            val executionIdToStackIndex = mutableMapOf<Long, Int>()

            threadMetrics.sortedBy { it.startTimeMs }.forEach { metric ->
                funcNames += metric.simpleName.intern()
                frameFunc += funcNames.lastIndex
                stackFrame += frameFunc.lastIndex
                stackPrefix += metric.parentExecutionId?.let { executionIdToStackIndex[it] }
                executionIdToStackIndex[metric.executionId] = stackFrame.lastIndex
                sampleStack += stackFrame.lastIndex
                sampleTime += (metric.startTimeMs - baseTimeMs).toDouble()
            }

            """
            |{"processType":"default",
            |"processStartupTime":0,"processShutdownTime":null,
            |"registerTime":0,"unregisterTime":null,"pausedRanges":[],
            |"name":"${threadName.escapeJson()}",
            |"isMainThread":${threadName == "main"},
            |"pid":$pid,"tid":$tid,
            |"samples":{
            |"weightType":"samples","weight":null,
            |"stack":${sampleStack.joinToString(",", "[", "]")},
            |"time":${sampleTime.joinToString(",", "[", "]")},
            |"length":${sampleStack.size}},
            |"markers":{"data":[],"name":[],"startTime":[],"endTime":[],"phase":[],"category":[],"length":0},
            |"stackTable":{
            |"frame":${stackFrame.joinToString(",", "[", "]")},
            |"prefix":${stackPrefix.joinToString(",", "[", "]") { it?.toString() ?: "null" }},
            |"length":${stackFrame.size}},
            |"frameTable":{
            |"address":${filledArray(frameFunc.size, "-1")},
            |"inlineDepth":${filledArray(frameFunc.size, "0")},
            |"category":${filledArray(frameFunc.size, "2")},
            |"subcategory":${filledArray(frameFunc.size, "0")},
            |"func":${frameFunc.joinToString(",", "[", "]")},
            |"nativeSymbol":${filledArray(frameFunc.size, "null")},
            |"innerWindowID":${filledArray(frameFunc.size, "null")},
            |"line":${filledArray(frameFunc.size, "null")},
            |"column":${filledArray(frameFunc.size, "null")},
            |"length":${frameFunc.size}},
            |"funcTable":{
            |"isJS":${filledArray(funcNames.size, "false")},
            |"relevantForJS":${filledArray(funcNames.size, "false")},
            |"name":${funcNames.joinToString(",", "[", "]")},
            |"resource":${filledArray(funcNames.size, "-1")},
            |"source":${filledArray(funcNames.size, "null")},
            |"lineNumber":${filledArray(funcNames.size, "null")},
            |"columnNumber":${filledArray(funcNames.size, "null")},
            |"length":${funcNames.size}},
            |"resourceTable":{"lib":[],"name":[],"host":[],"type":[],"length":0},
            |"nativeSymbols":{"libIndex":[],"address":[],"name":[],"functionSize":[],"length":0}}
            """.trimMargin().replace("\n", "")
        }

        val stringArrayJson = stringArray.joinToString(",", "[", "]") { "\"${it.escapeJson()}\"" }

        writer.write("""
            |{"meta":{
            |"interval":0,"startTime":0,"processType":0,
            |"categories":[
            |{"name":"Other","color":"grey","subcategories":["Other"]},
            |{"name":"Native","color":"magenta","subcategories":["Other"]},
            |{"name":"Java","color":"green","subcategories":["Other"]},
            |{"name":"System","color":"yellow","subcategories":["Other"]},
            |{"name":"Kernel","color":"orange","subcategories":["Other"]}],
            |"product":"${logName.escapeJson()}",
            |"stackwalk":0,"version":30,"preprocessedProfileVersion":58,
            |"symbolicationNotSupported":true,"markerSchema":[],
            |"platform":"Android","toolkit":"android","importedFrom":"Demeter",
            |"usesOnlyOneStackType":true,"sourceCodeIsNotOnSearchfox":true,
            |"keepProfileThreadOrder":true},
            |"libs":[],
            |"shared":{"stringArray":$stringArrayJson,"sources":{"uuid":[],"filename":[],"length":0}},
            |"threads":[$threadsJson]}
        """.trimMargin().replace("\n", ""))
    }

    Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, context.getUriForFile(sharedFile))
        type = "application/json"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.let { context.startActivity(Intent.createChooser(it, null)) }
}

private fun String.escapeJson(): String = buildString {
    for (char in this@escapeJson) {
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
}
