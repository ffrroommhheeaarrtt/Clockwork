package org.fromheart.clockwork.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.launch
import org.fromheart.clockwork.ACTION_ALARM_FRAGMENT
import org.fromheart.clockwork.ACTION_TIMER_FRAGMENT
import org.fromheart.clockwork.adapter.TimerAdapter
import org.fromheart.clockwork.app
import org.fromheart.clockwork.data.model.Timer
import org.fromheart.clockwork.data.model.TimerStatus
import org.fromheart.clockwork.databinding.FragmentTimerBinding
import org.fromheart.clockwork.getTimerTime
import org.fromheart.clockwork.repository.TimerRepository
import org.fromheart.clockwork.viewmodel.TimerViewModel
import org.fromheart.clockwork.viewmodel.TimerViewModelFactory

class TimerFragment : Fragment(), TimerAdapter.TimerListener {

    private val viewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(requireActivity().application.app, TimerRepository(requireActivity().application.app.database.timerDao()))
    }

    private lateinit var binding: FragmentTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (requireActivity().intent.action) {
            ACTION_ALARM_FRAGMENT -> {
                requireActivity().intent.action = null
                findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToAlarmFragment())
            }
            ACTION_TIMER_FRAGMENT -> requireActivity().intent.action = null
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finish() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TimerAdapter(this)
        binding.apply {
            (timerRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter.timerTouchHelper.attachToRecyclerView(timerRecycler)
            timerRecycler.adapter = adapter

            timerFab.setOnClickListener {
                findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerKeyboardFragment())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timerFlow.collect { adapter.submitList(it) }
        }
    }

    override fun onTimeButtonClicked(timer: Timer) {
        if (timer.status == TimerStatus.STOP.number) {
            viewModel.setTimerToUpdate(timer)
            findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerKeyboardFragment())
        }
    }

    override fun onStartButtonClicked(timer: Timer) {
        viewModel.changeTimerState(
            timer.copy(status = if (timer.status == TimerStatus.START.number) TimerStatus.PAUSE.number else TimerStatus.START.number)
        )
    }

    override fun onStopButtonClicked(timer: Timer) {
        viewModel.changeTimerState(
            timer.copy(
                status = TimerStatus.STOP.number,
                time = getTimerTime(timer.hour, timer.minute, timer.second)
            )
        )
    }

    override fun onSwiped(timer: Timer) {
        viewModel.deleteTimer(timer)
    }
}