package org.fromheart.clockwork.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.data.model.TimeZoneModel
import org.fromheart.clockwork.databinding.ItemTimeZoneBinding

class TimeZoneAdapter(private val timeZoneListener: TimeZoneListener)
    : ListAdapter<TimeZoneModel, TimeZoneAdapter.TimeZoneViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneViewHolder {
        val binding = ItemTimeZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeZoneViewHolder(binding, timeZoneListener)
    }

    override fun onBindViewHolder(holder: TimeZoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface TimeZoneListener {
        fun onItemClicked(timeZone: TimeZoneModel)
        fun onTimeTextViewBound(timeZone: TimeZoneModel, textView: TextView)
    }

    class TimeZoneViewHolder(
        private val binding: ItemTimeZoneBinding,
        private val timeZoneListener: TimeZoneListener
        ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(timeZone: TimeZoneModel) = binding.apply {
            zoneTextView.text = timeZone.zone
            itemView.setOnClickListener { timeZoneListener.onItemClicked(timeZone) }
            timeZoneListener.onTimeTextViewBound(timeZone, timeTextView)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<TimeZoneModel>() {

            override fun areItemsTheSame(oldItem: TimeZoneModel, newItem: TimeZoneModel): Boolean = oldItem.zone == newItem.zone

            override fun areContentsTheSame(oldItem: TimeZoneModel, newItem: TimeZoneModel): Boolean = oldItem == newItem
        }
    }
}