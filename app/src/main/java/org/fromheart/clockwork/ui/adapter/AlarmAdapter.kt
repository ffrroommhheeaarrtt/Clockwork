package org.fromheart.clockwork.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import org.fromheart.clockwork.data.model.AlarmModel
import org.fromheart.clockwork.databinding.ItemAlarmBinding
import org.fromheart.clockwork.util.formatTime

class AlarmAdapter(private val alarmListener: AlarmListener) : ListAdapter<AlarmModel, AlarmAdapter.AlarmViewHolder>(DiffCallback) {

    val alarmTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                alarmListener.onSwiped(getItem(viewHolder.adapterPosition))
            }
        })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding, alarmListener)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface AlarmListener {
        fun onItemClicked(alarm: AlarmModel)
        fun onTimeButtonClicked(alarm: AlarmModel)
        fun onSwitched(alarm: AlarmModel)
        fun onCheckedStateChangeWeekChipGroup(alarm: AlarmModel): (ChipGroup, List<Int>) -> Unit
        fun onSwiped(alarm: AlarmModel)
    }

    class AlarmViewHolder(
        private val binding: ItemAlarmBinding,
        private val alarmListener: AlarmListener
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: AlarmModel) = binding.apply {
            itemView.setOnClickListener { alarmListener.onItemClicked(alarm) }
            timeButton.text = formatTime(alarm.hour, alarm.minute)
            timeButton.setOnClickListener { alarmListener.onTimeButtonClicked(alarm) }
            alarmSwitch.isChecked = alarm.status
            alarmSwitch.setOnClickListener { alarmListener.onSwitched(alarm) }
            daysOfWeekText.text = alarm.daysLabel
            weekChipGroup.visibility = if (alarm.open) View.VISIBLE else View.GONE
            weekChipGroup.setOnCheckedStateChangeListener(alarmListener.onCheckedStateChangeWeekChipGroup(alarm))
            mondayChip.isChecked = alarm.daysSet.contains(0)
            tuesdayChip.isChecked = alarm.daysSet.contains(1)
            wednesdayChip.isChecked = alarm.daysSet.contains(2)
            thursdayChip.isChecked = alarm.daysSet.contains(3)
            fridayChip.isChecked = alarm.daysSet.contains(4)
            saturdayChip.isChecked = alarm.daysSet.contains(5)
            sundayChip.isChecked = alarm.daysSet.contains(6)
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<AlarmModel>() {

            override fun areItemsTheSame(oldItem: AlarmModel, newItem: AlarmModel): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: AlarmModel, newItem: AlarmModel): Boolean = oldItem == newItem
        }
    }
}