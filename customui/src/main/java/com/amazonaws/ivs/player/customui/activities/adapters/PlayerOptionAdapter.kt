package com.amazonaws.ivs.player.customui.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.customui.data.entity.OptionDataItem
import com.amazonaws.ivs.player.customui.databinding.SelectorListItemBinding
import kotlin.properties.Delegates

/**
 * Player quality and rate option selection adapter
 */
class PlayerOptionAdapter(
    private val callback: PlayerOptionCallback
) : RecyclerView.Adapter<PlayerOptionAdapter.ViewHolder>() {

    var items: List<OptionDataItem> by Delegates.observable(emptyList()) { _, old, new ->
        DiffUtil.calculateDiff(PlayerOptionDiff(old, new)).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SelectorListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = items[position]
    }

    inner class ViewHolder(val binding: SelectorListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                callback.onOptionClicked(adapterPosition)
            }
        }
    }

    interface PlayerOptionCallback {
        fun onOptionClicked(position: Int)
    }

    inner class PlayerOptionDiff(private val oldItems: List<OptionDataItem>,
                             private val newItems: List<OptionDataItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].option == newItems[newItemPosition].option
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }
}
