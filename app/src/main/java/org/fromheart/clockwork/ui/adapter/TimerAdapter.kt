package org.fromheart.clockwork.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.model.TimerModel
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.databinding.ItemTimerBinding

class TimerAdapter(private val timerListener: TimerListener) : ListAdapter<TimerModel, TimerAdapter.TimerViewHolder>(DiffCallback) {

    val timerTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                timerListener.onSwiped(getItem(viewHolder.adapterPosition))
            }
        })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val binding = ItemTimerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimerViewHolder(binding, timerListener)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface TimerListener {
        fun onTimeButtonClicked(timer: TimerModel)
        fun onStartButtonClicked(timer: TimerModel)
        fun onStopButtonClicked(timer: TimerModel)
        fun onSwiped(timer: TimerModel)
        fun onTimeButtonBound(timer: TimerModel, button: Button)
    }

    class TimerViewHolder(
        private val binding: ItemTimerBinding,
        private val timerListener: TimerListener
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timer: TimerModel) = binding.apply {
            timerListener.onTimeButtonBound(timer, timeButton)
            timeButton.setOnClickListener { timerListener.onTimeButtonClicked(timer) }
            startButton.setImageResource(if (timer.state == TimerState.STARTED) R.drawable.ic_pause else R.drawable.ic_play)
            startButton.setOnClickListener { timerListener.onStartButtonClicked(timer) }
            stopButton.visibility = if (timer.state == TimerState.STOPPED) View.INVISIBLE else View.VISIBLE
            stopButton.setOnClickListener { timerListener.onStopButtonClicked(timer) }
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<TimerModel>() {

            override fun areItemsTheSame(oldItem: TimerModel, newItem: TimerModel): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TimerModel, newItem: TimerModel): Boolean = oldItem == newItem
        }
    }
}