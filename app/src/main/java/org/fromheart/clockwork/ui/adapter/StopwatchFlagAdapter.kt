package org.fromheart.clockwork.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.data.model.StopwatchFlag
import org.fromheart.clockwork.databinding.ItemStopwatchFlagBinding
import org.fromheart.clockwork.util.getFormattedStopwatchTime

class StopwatchFlagAdapter : ListAdapter<StopwatchFlag, StopwatchFlagAdapter.StopwatchFlagViewHolder>(
    DiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchFlagViewHolder {
        return StopwatchFlagViewHolder(ItemStopwatchFlagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: StopwatchFlagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StopwatchFlagViewHolder(private val binding: ItemStopwatchFlagBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(stopwatchFlag: StopwatchFlag) = binding.apply {
            flagTextView.text = stopwatchFlag.id.toString()
            timeDifferenceTextView.text = "+${getFormattedStopwatchTime(stopwatchFlag.timeDifference)}"
            flagTimeTextView.text = getFormattedStopwatchTime(stopwatchFlag.flagTime)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<StopwatchFlag>() {

            override fun areItemsTheSame(oldItem: StopwatchFlag, newItem: StopwatchFlag): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StopwatchFlag, newItem: StopwatchFlag): Boolean {
                return oldItem == newItem
            }
        }
    }
}