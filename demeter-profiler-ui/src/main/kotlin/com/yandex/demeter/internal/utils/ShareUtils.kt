package com.yandex.demeter.internal.utils

import android.content.Context
import android.content.Intent
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.getUriForFile
import com.yandex.demeter.internal.model.TimeMetric
import java.io.BufferedWriter
import java.io.File
import java.io.Writer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val CURRENT_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

private const val CSV_COLUMNS_DELIMITER = ','
private const val INJECT_PATH_SEPARATOR = ';'

@InternalDemeterApi
fun shareCsv(context: Context, metrics: Collection<TimeMetric>, logName: String) {
    val directory = context.getExternalFilesDir("external_path")
    val sharedFile = checkFileForPathTraversal(
        File(directory, "$logName (${getCurrentDateTime()}).csv"),
        directory
    )

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
    val currentDateTime = LocalDateTime.now()
    return CURRENT_TIME_FORMATTER.format(currentDateTime)
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
