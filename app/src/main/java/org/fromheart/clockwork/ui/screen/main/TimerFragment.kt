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
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.Timer
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.databinding.FragmentTimerBinding
import org.fromheart.clockwork.service.TimerService
import org.fromheart.clockwork.ui.adapter.TimerAdapter
import org.fromheart.clockwork.ui.viewmodel.TimerViewModel
import org.fromheart.clockwork.util.getFormattedTimerTime
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TimerFragment : Fragment(), TimerAdapter.TimerListener {

    private val viewModel: TimerViewModel by sharedViewModel()

    private lateinit var binding: FragmentTimerBinding

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
            (timerRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
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

    override fun onTimeButtonClicked(timer: Timer) {
        if (timer.state == TimerState.STOPPED) {
            viewModel.updateTimerKeyboard(timer)
            findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerKeyboardFragment(timer.id))
        }
    }

    override fun onStartButtonClicked(timer: Timer) {
        viewModel.playButtonClicked(timer)
    }

    override fun onStopButtonClicked(timer: Timer) {
        viewModel.stopTimer(timer)
    }

    override fun onSwiped(timer: Timer) {
        viewModel.deleteTimer(timer)
    }

    override fun onTimeButtonBound(timer: Timer, button: Button) {
        if (timer.state == TimerState.STARTED) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.timerChannelMap[timer.id]?.receiveAsFlow()?.collect {
                        button.text = getFormattedTimerTime(it)
                    }
                }
            }
        } else {
            button.text = getFormattedTimerTime(timer.time)
        }
    }
}