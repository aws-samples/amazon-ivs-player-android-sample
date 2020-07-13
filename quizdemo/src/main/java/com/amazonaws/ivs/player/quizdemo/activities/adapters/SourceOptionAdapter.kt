package com.amazonaws.ivs.player.quizdemo.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.quizdemo.data.entity.SourceDataItem
import com.amazonaws.ivs.player.quizdemo.databinding.SourceItemBinding
import kotlin.properties.Delegates

/**
 * Player source option selection adapter
 */
class SourceOptionAdapter(
    private val callback: PlayerOptionCallback
) : RecyclerView.Adapter<SourceOptionAdapter.ViewHolder>() {

    var items: List<SourceDataItem> by Delegates.observable(emptyList()) { _, old, new ->
        DiffUtil.calculateDiff(SourceOptionDiff(old, new)).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SourceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = items[position]
        val dataItem = items[position]

        holder.binding.sourceName.setOnClickListener {
            callback.onOptionClicked(dataItem.url)
        }

        holder.binding.deleteBtn.setOnClickListener {
            callback.onOptionDelete(dataItem.url)
        }
    }

    inner class ViewHolder(val binding: SourceItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface PlayerOptionCallback {

        fun onOptionClicked(url: String)

        fun onOptionDelete(url: String)
    }

    inner class SourceOptionDiff(
        private val oldItems: List<SourceDataItem>,
        private val newItems: List<SourceDataItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].url == newItems[newItemPosition].url
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

}
