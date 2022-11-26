package org.fromheart.clockwork.ui.screen.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.launch
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.model.StopwatchState
import org.fromheart.clockwork.databinding.FragmentStopwatchBinding
import org.fromheart.clockwork.service.StopwatchService
import org.fromheart.clockwork.ui.adapter.StopwatchFlagAdapter
import org.fromheart.clockwork.ui.viewmodel.StopwatchViewModel
import org.fromheart.clockwork.util.ACTION_SET_STOPWATCH_FLAG
import org.fromheart.clockwork.util.getFormattedStopwatchTime
import org.koin.androidx.viewmodel.ext.android.viewModel

class StopwatchFragment: Fragment() {

    private val viewModel: StopwatchViewModel by viewModel()

    private lateinit var binding: FragmentStopwatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finish() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStopwatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = StopwatchFlagAdapter()

        binding.apply {
            (stopwatchFlagRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            stopwatchFlagRecycler.adapter = adapter

            startFab.setOnClickListener { viewModel.playButtonClicked() }
            stopFab.setOnClickListener { viewModel.stopStopwatch() }
            flagFab.setOnClickListener {
                ContextCompat.startForegroundService(requireContext(), Intent(requireContext(), StopwatchService::class.java)
                    .setAction(ACTION_SET_STOPWATCH_FLAG))
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.flagFlow.collect {
                    adapter.submitList(it) { stopwatchFlagRecycler.scrollToPosition(0) }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.pauseTimeFlow.collect {
                        stopwatchTimeTextView.text = getFormattedStopwatchTime(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.timeReceiverFlow.collect {
                        stopwatchTimeTextView.text = getFormattedStopwatchTime(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.stopwatchState.collect { state ->
                        when (state) {
                            StopwatchState.STARTED -> {
                                ContextCompat
                                    .startForegroundService(requireContext(), Intent(requireContext(), StopwatchService::class.java))
                                startFab.setImageResource(R.drawable.ic_pause)
                                stopFab.visibility = View.VISIBLE
                                flagFab.visibility = View.VISIBLE
                            }
                            StopwatchState.PAUSED -> {
                                startFab.setImageResource(R.drawable.ic_play)
                                flagFab.visibility = View.INVISIBLE
                            }
                            StopwatchState.STOPPED -> {
                                startFab.setImageResource(R.drawable.ic_play)
                                stopFab.visibility = View.INVISIBLE
                                flagFab.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
        }
    }
}