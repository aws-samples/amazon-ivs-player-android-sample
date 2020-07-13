package com.amazonaws.ivs.player.quizdemo.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.quizdemo.databinding.AnswerListItemBinding
import com.amazonaws.ivs.player.quizdemo.models.AnswerViewItem
import kotlin.properties.Delegates

class AnswerAdapter(private val onAnswerClicked: (position: Int) -> Unit) :
    RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {

    var items: List<AnswerViewItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AnswerListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = items[position]
        holder.binding.optionItem.setOnClickListener {
            onAnswerClicked(position)
        }
    }

    inner class ViewHolder(
        val binding: AnswerListItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

}
