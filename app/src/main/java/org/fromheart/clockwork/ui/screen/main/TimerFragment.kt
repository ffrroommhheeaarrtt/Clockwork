package org.fromheart.clockwork.ui.screen.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.TimerModel
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.databinding.FragmentTimerBinding
import org.fromheart.clockwork.service.TimerService
import org.fromheart.clockwork.ui.adapter.TimerAdapter
import org.fromheart.clockwork.ui.viewmodel.TimerViewModel
import org.fromheart.clockwork.util.disableSimpleItemAnimator
import org.fromheart.clockwork.util.formatTimerTime
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class TimerFragment : Fragment(), TimerAdapter.TimerListener {

    private val viewModel: TimerViewModel by activityViewModel()

    private lateinit var binding: FragmentTimerBinding

    private val timerJobMap = mutableMapOf<Long, Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finish() }

        requireContext().startService(Intent(requireContext(), TimerService::class.java))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TimerAdapter(this)

        binding.apply {
            timerRecycler.disableSimpleItemAnimator()
            adapter.timerTouchHelper.attachToRecyclerView(timerRecycler)
            timerRecycler.adapter = adapter

            timerFab.setOnClickListener {
                viewModel.resetTimerKeyboard()
                findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerKeyboardFragment())
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.timerFlow.collect { adapter.submitList(it) }
            }
        }
    }

    override fun onTimeButtonClicked(timer: TimerModel) {
        if (timer.state == TimerState.STOPPED) {
            viewModel.updateTimerKeyboard(timer)
            findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerKeyboardFragment(timer.id))
        }
    }

    override fun onStartButtonClicked(timer: TimerModel) {
        viewModel.playButtonClicked(timer)
    }

    override fun onStopButtonClicked(timer: TimerModel) {
        viewModel.stopTimer(timer)
    }

    override fun onSwiped(timer: TimerModel) {
        viewModel.deleteTimer(timer)
    }

    override fun onTimeButtonBound(timer: TimerModel, button: Button) {
        if (timer.state == TimerState.STARTED) {
            timerJobMap[timer.id] = viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.timerChannelMap[timer.id]?.receiveAsFlow()?.collect {
                        button.text = formatTimerTime(it)
                    }
                }
            }
        } else {
            timerJobMap[timer.id]?.cancel()
            timerJobMap.remove(timer.id)
            button.text = formatTimerTime(timer.time)
        }
    }
}