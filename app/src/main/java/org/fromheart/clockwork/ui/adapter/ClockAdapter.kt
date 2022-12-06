package org.fromheart.clockwork.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.data.model.TimeZoneEntity
import org.fromheart.clockwork.databinding.ItemClockBinding
import org.fromheart.clockwork.util.formatTimeZoneDifference
import java.util.*

class ClockAdapter(private val clockListener: ClockListener) : ListAdapter<TimeZoneEntity, ClockAdapter.ClockViewHolder>(DiffCallback) {

    val clockTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                clockListener.onSwiped(getItem(viewHolder.adapterPosition))
            }
        })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClockViewHolder {
        val binding = ItemClockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClockViewHolder(binding, clockListener)
    }

    override fun onBindViewHolder(holder: ClockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface ClockListener {
        fun onTimeTextViewBound(timeZone: TimeZoneEntity, textView: TextView)
        fun onSwiped(timeZone: TimeZoneEntity)
    }

    class ClockViewHolder(
        private val binding: ItemClockBinding,
        private val clockListener: ClockListener
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timeZone: TimeZoneEntity) = binding.apply {
            cityTextView.text = timeZone.zoneName
            timeDifferenceTextView.text = formatTimeZoneDifference(TimeZone.getTimeZone(timeZone.id))
            clockListener.onTimeTextViewBound(timeZone, timeTextView)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<TimeZoneEntity>() {

            override fun areItemsTheSame(oldItem: TimeZoneEntity, newItem: TimeZoneEntity): Boolean = oldItem.zoneName == newItem.zoneName

            override fun areContentsTheSame(oldItem: TimeZoneEntity, newItem: TimeZoneEntity): Boolean = oldItem == newItem
        }
    }
}