package org.fromheart.clockwork.ui.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.ChipGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.model.AlarmEntity
import org.fromheart.clockwork.databinding.FragmentAlarmBinding
import org.fromheart.clockwork.ui.adapter.AlarmAdapter
import org.fromheart.clockwork.ui.viewmodel.AlarmViewModel
import org.fromheart.clockwork.util.disableSimpleItemAnimator
import org.fromheart.clockwork.util.getAlarmTime
import org.fromheart.clockwork.util.getDaysLabel
import org.fromheart.clockwork.util.getNextAlarmTime
import org.koin.androidx.viewmodel.ext.android.viewModel

class AlarmFragment : Fragment(), AlarmAdapter.AlarmListener {

    private val viewModel: AlarmViewModel by viewModel()

    private lateinit var binding: FragmentAlarmBinding

    private fun createTimePicker(hour: Int = 0, minute: Int = 0): MaterialTimePicker = MaterialTimePicker.Builder().run {
        setHour(hour)
        setMinute(minute)
        setTitleText(R.string.title_select_time)
        setNegativeButtonText(R.string.button_cancel)
        setPositiveButtonText(R.string.button_ok)
        setTimeFormat(TimeFormat.CLOCK_24H)
        setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
        build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finish() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AlarmAdapter(this)

        binding.apply {
            alarmRecycler.disableSimpleItemAnimator()
            alarmRecycler.adapter = adapter
            adapter.alarmTouchHelper.attachToRecyclerView(alarmRecycler)

            alarmFab.setOnClickListener {
                val picker = createTimePicker()
                picker.addOnPositiveButtonClickListener {
                    viewModel.addAlarm(
                        AlarmEntity(
                            hour = picker.hour,
                            minute = picker.minute,
                            time = getAlarmTime(picker.hour, picker.minute),
                            daysLabel = requireContext().getDaysLabel(picker.hour, picker.minute)
                        )
                    )
                }
                picker.show(childFragmentManager, "time_picker")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alarmFlow.collect { adapter.submitList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentDayFlow.collect { if (it) viewModel.updateAlarmDays() }
            }
        }
    }

    override fun onItemClicked(alarm: AlarmEntity) {
        viewModel.itemClicked(alarm)
    }

    override fun onTimeButtonClicked(alarm: AlarmEntity) {
        if (!alarm.open) viewModel.itemClicked(alarm)
        val picker = createTimePicker(alarm.hour, alarm.minute)
        picker.addOnPositiveButtonClickListener {
            val newAlarm = alarm.copy(
                hour = picker.hour,
                minute = picker.minute,
                time = getNextAlarmTime(picker.hour, picker.minute, alarm.daysSet),
                daysLabel = if (alarm.daysSet.isEmpty()) requireContext().getDaysLabel(picker.hour, picker.minute) else alarm.daysLabel,
                open = true,
                status = true
            )
            viewModel.updateAndSetAlarm(newAlarm)
        }
        picker.show(childFragmentManager, "time_picker")
    }

    override fun onCheckedStateChangeWeekChipGroup(alarm: AlarmEntity): (ChipGroup, List<Int>) -> Unit {
        return { group, list ->
            val days = mutableSetOf<Int>()
            for (id in list) {
                group.forEachIndexed { i, chip ->
                    if (id == chip.id) days.add(i)
                }
            }

            val newAlarm = alarm.copy(
                time = if (alarm.status) getNextAlarmTime(alarm.hour, alarm.minute, days) else 0,
                daysLabel = when (days.size) {
                    0 -> if (alarm.status) requireContext().getDaysLabel(alarm.hour, alarm.minute) else ""
                    1 -> resources.getStringArray(R.array.week)[days.first()]
                    7 -> getString(R.string.every_day)
                    else -> {
                        val array = resources.getStringArray(R.array.week_abb)
                        val str = mutableListOf<String>()
                        for (i in days) { str.add(array[i]) }
                        str.joinToString(", ")
                    }
                },
                daysSet = days
            )
            if (alarm.status) viewModel.updateAndSetAlarm(newAlarm) else viewModel.updateAlarm(newAlarm)
        }
    }

    override fun onSwitched(alarm: AlarmEntity) {
        if (alarm.status) {
            viewModel.updateAndSetAlarm(
                alarm.copy(
                    status = false,
                    daysLabel = if (alarm.daysSet.isEmpty()) "" else alarm.daysLabel
                )
            )
        }
        else {
            viewModel.updateAndSetAlarm(
                alarm.copy(
                    status = true,
                    time = getNextAlarmTime(alarm.hour, alarm.minute, alarm.daysSet),
                    daysLabel = if (alarm.daysSet.isEmpty()) requireContext().getDaysLabel(alarm.hour, alarm.minute) else alarm.daysLabel
                )
            )
        }
    }

    override fun onSwiped(alarm: AlarmEntity) {
        viewModel.deleteAlarm(alarm)
    }
}