package org.fromheart.clockwork.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.data.model.StopwatchFlagEntity
import org.fromheart.clockwork.databinding.ItemStopwatchFlagBinding
import org.fromheart.clockwork.util.formatStopwatchTime

class StopwatchFlagAdapter : ListAdapter<StopwatchFlagEntity, StopwatchFlagAdapter.StopwatchFlagViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchFlagViewHolder {
        return StopwatchFlagViewHolder(ItemStopwatchFlagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: StopwatchFlagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StopwatchFlagViewHolder(private val binding: ItemStopwatchFlagBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(stopwatchFlag: StopwatchFlagEntity) = binding.apply {
            flagTextView.text = stopwatchFlag.id.toString()
            timeDifferenceTextView.text = "+${formatStopwatchTime(stopwatchFlag.timeDifference)}"
            flagTimeTextView.text = formatStopwatchTime(stopwatchFlag.flagTime)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<StopwatchFlagEntity>() {

            override fun areItemsTheSame(oldItem: StopwatchFlagEntity, newItem: StopwatchFlagEntity): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: StopwatchFlagEntity, newItem: StopwatchFlagEntity): Boolean = oldItem == newItem
        }
    }
}