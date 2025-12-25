package com.yandex.demeter.profiler.inject.ui.internal

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.yandex.demeter.internal.WarningLevel
import com.yandex.demeter.internal.WarningLevel.First
import com.yandex.demeter.internal.WarningLevel.Second
import com.yandex.demeter.internal.WarningLevel.Third
import com.yandex.demeter.internal.WarningLevel.Zero
import com.yandex.demeter.internal.model.TimeMetricViewItem
import com.yandex.demeter.profiler.ui.R
import com.yandex.demeter.profiler.ui.databinding.AdmListItemMetricsDescriptionBinding
import com.yandex.demeter.profiler.ui.databinding.AdmListItemMetricsHeaderBinding

internal class ExpandableMetricsListAdapter : BaseExpandableListAdapter() {
    private val metricViewDescriptionList: MutableList<TimeMetricViewItem> = ArrayList()

    fun updateMetrics(metricViewDescriptions: List<TimeMetricViewItem>) {
        metricViewDescriptionList.clear()
        metricViewDescriptionList.addAll(metricViewDescriptions)
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int {
        return metricViewDescriptionList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return metricViewDescriptionList[groupPosition].args.size
    }

    override fun getGroup(groupPosition: Int): TimeMetricViewItem {
        return metricViewDescriptionList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): TimeMetricViewItem {
        return metricViewDescriptionList[groupPosition].args[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return 0
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val viewHolder: HeaderViewHolder
        if (convertView == null) {
            convertView =
                AdmListItemMetricsHeaderBinding.inflate(LayoutInflater.from(parent.context)).root
            viewHolder = HeaderViewHolder(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as HeaderViewHolder
        }

        val metricDescription = getGroup(groupPosition)
        viewHolder.bindView(metricDescription)

        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertedView = convertView

        val viewHolder: DescriptionViewHolder
        if (convertedView == null) {
            convertedView =
                AdmListItemMetricsDescriptionBinding.inflate(LayoutInflater.from(parent.context)).root
            viewHolder = DescriptionViewHolder(convertedView)
            convertedView.tag = viewHolder
        } else {
            viewHolder = convertedView.tag as DescriptionViewHolder
        }
        val metricDescription = getChild(groupPosition, childPosition)
        viewHolder.bindView(metricDescription)

        return convertedView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    private class HeaderViewHolder(private val root: View) {
        private val tvClassName: TextView = root.findViewById(R.id.tvClassName)
        private val tvInitTime: TextView = root.findViewById(R.id.tvInitTime)

        fun bindView(metricViewDescription: TimeMetricViewItem) {
            tvClassName.text = metricViewDescription.simpleName
            tvInitTime.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(metricViewDescription.description, 0)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(metricViewDescription.description)
            }
            val resources = tvClassName.context.resources

            when (WarningLevel.getLevel(metricViewDescription.totalInitTime)) {
                Zero -> {
                    root.setBackgroundResource(R.color.d2m_transparent)
                    tvInitTime.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_default_description, null))
                    tvClassName.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_default_title, null))
                }

                First -> {
                    root.setBackgroundResource(R.color.d2m_bg_warning_1)
                    tvClassName.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_1_and_2, null))
                    tvInitTime.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_1_and_2, null))
                }

                Second -> {
                    root.setBackgroundResource(R.color.d2m_bg_warning_2)
                    tvClassName.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_1_and_2, null))
                    tvInitTime.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_1_and_2, null))
                }

                Third -> {
                    root.setBackgroundResource(R.color.d2m_bg_warning_3)
                    tvClassName.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_3, null))
                    tvInitTime.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_3, null))
                }
            }
        }
    }

    private class DescriptionViewHolder(view: View) {
        private val tvTreeDescription: TextView = view.findViewById(R.id.tvTreeDescription)

        fun bindView(metricDescription: TimeMetricViewItem) {
            val resources = tvTreeDescription.context.resources
            val description = metricDescription.description
            tvTreeDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(description, 0)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(description)
            }
            when (WarningLevel.getLevel(metricDescription.totalInitTime)) {
                Zero -> {
                    tvTreeDescription.setBackgroundResource(R.color.d2m_transparent)
                    tvTreeDescription.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_default_description, null))
                }

                First -> {
                    tvTreeDescription.setBackgroundResource(R.color.d2m_bg_warning_1)
                    tvTreeDescription.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_1_and_2, null))
                }

                Second -> {
                    tvTreeDescription.setBackgroundResource(R.color.d2m_bg_warning_2)
                    tvTreeDescription.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_1_and_2, null))
                }

                Third -> {
                    tvTreeDescription.setBackgroundResource(R.color.d2m_bg_warning_3)
                    tvTreeDescription.setTextColor(ResourcesCompat.getColor(resources, R.color.d2m_font_warning_3, null))
                }
            }
        }
    }
}
