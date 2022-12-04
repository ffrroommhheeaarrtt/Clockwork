package org.fromheart.clockwork.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.data.model.TimeZoneModel
import org.fromheart.clockwork.databinding.ItemClockBinding
import org.fromheart.clockwork.util.formatTimeZoneDifference
import java.util.*

class ClockAdapter(private val clockListener: ClockListener) : ListAdapter<TimeZoneModel, ClockAdapter.ClockViewHolder>(DiffCallback) {

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
        fun onTimeTextViewBound(timeZone: TimeZoneModel, textView: TextView)
        fun onSwiped(timeZone: TimeZoneModel)
    }

    class ClockViewHolder(
        private val binding: ItemClockBinding,
        private val clockListener: ClockListener
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timeZone: TimeZoneModel) = binding.apply {
            cityTextView.text = timeZone.zone
            timeDifferenceTextView.text = formatTimeZoneDifference(TimeZone.getTimeZone(timeZone.id))
            clockListener.onTimeTextViewBound(timeZone, timeTextView)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<TimeZoneModel>() {

            override fun areItemsTheSame(oldItem: TimeZoneModel, newItem: TimeZoneModel): Boolean = oldItem.zone == newItem.zone

            override fun areContentsTheSame(oldItem: TimeZoneModel, newItem: TimeZoneModel): Boolean = oldItem == newItem
        }
    }
}