package org.fromheart.clockwork.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.data.model.TimeZoneEntity
import org.fromheart.clockwork.databinding.ItemTimeZoneBinding

class TimeZoneAdapter(private val timeZoneListener: TimeZoneListener)
    : ListAdapter<TimeZoneEntity, TimeZoneAdapter.TimeZoneViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneViewHolder {
        val binding = ItemTimeZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeZoneViewHolder(binding, timeZoneListener)
    }

    override fun onBindViewHolder(holder: TimeZoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface TimeZoneListener {
        fun onItemClicked(timeZone: TimeZoneEntity)
        fun onTimeTextViewBound(timeZone: TimeZoneEntity, textView: TextView)
    }

    class TimeZoneViewHolder(
        private val binding: ItemTimeZoneBinding,
        private val timeZoneListener: TimeZoneListener
        ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(timeZone: TimeZoneEntity) = binding.apply {
            zoneTextView.text = timeZone.zoneName
            itemView.setOnClickListener { timeZoneListener.onItemClicked(timeZone) }
            timeZoneListener.onTimeTextViewBound(timeZone, timeTextView)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<TimeZoneEntity>() {

            override fun areItemsTheSame(oldItem: TimeZoneEntity, newItem: TimeZoneEntity): Boolean = oldItem.zoneName == newItem.zoneName

            override fun areContentsTheSame(oldItem: TimeZoneEntity, newItem: TimeZoneEntity): Boolean = oldItem == newItem
        }
    }
}