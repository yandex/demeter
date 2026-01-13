package com.yandex.demeter.profiler.tracer.ui.internal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.profiler.tracer.internal.data.db.TraceMetricEntity
import com.yandex.demeter.profiler.ui.R

internal class TraceMetricPagingAdapter :
    PagingDataAdapter<TraceMetricEntity, TraceMetricPagingAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adm_list_item_trace_metrics, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

    fun getItemName(position: Int): String? {
        return getItem(position)?.simpleName
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tvClassName)
        private val description: TextView = view.findViewById(R.id.tvInitTime)

        fun bind(item: TraceMetricEntity) {
            val (backgroundRes, textColor) = getColors(item.maxDurationMs)
            name.text = item.simpleName
            description.text = HtmlCompat.fromHtml(
                item.colorizeDescription(),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            itemView.setBackgroundResource(backgroundRes)
            description.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        private fun getColors(maxDurationMs: Long): Pair<Int, Int> {
            return when (WarningLevel.getLevel(maxDurationMs)) {
                WarningLevel.Zero -> R.color.d2m_transparent to R.color.d2m_font_default_description
                WarningLevel.First -> R.color.d2m_bg_warning_1 to R.color.d2m_font_warning_1_and_2
                WarningLevel.Second -> R.color.d2m_bg_warning_2 to R.color.d2m_font_warning_1_and_2
                WarningLevel.Third -> R.color.d2m_bg_warning_3 to R.color.d2m_font_warning_3
            }
        }

        private fun TraceMetricEntity.colorizeDescription(): String {
            val warningLevel = WarningLevel.getLevel(maxDurationMs)
            return buildString {
                append("Max time: ")
                if (warningLevel != WarningLevel.Zero) {
                    append("<b><font color='#9C27B0'>")
                }
                append(maxDurationMs)
                append("ms, ")
                if (warningLevel != WarningLevel.Zero) {
                    append("</font></b>")
                }
                append("entered ")
                append(count)
                append(" times")
                append(" <font color='#00BFA5'>($lastThreadName)</font>")
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TraceMetricEntity>() {
            override fun areItemsTheSame(
                oldItem: TraceMetricEntity,
                newItem: TraceMetricEntity
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: TraceMetricEntity,
                newItem: TraceMetricEntity
            ): Boolean = oldItem == newItem
        }
    }
}
