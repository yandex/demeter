package com.yandex.demeter.internal.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yandex.demeter.api.UiDemeterPlugin

internal class MetricsAdapter : Adapter<ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<UiDemeterPlugin>() {
        override fun areItemsTheSame(
            oldItem: UiDemeterPlugin,
            newItem: UiDemeterPlugin,
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: UiDemeterPlugin,
            newItem: UiDemeterPlugin,
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return object : ViewHolder(differ.currentList[viewType].ui(parent.context)) {}
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit = Unit

    override fun getItemCount(): Int = differ.currentList.size

    fun updateTabs(tabs: List<UiDemeterPlugin>): Unit = differ.submitList(tabs)

    override fun getItemViewType(position: Int): Int = position
}
