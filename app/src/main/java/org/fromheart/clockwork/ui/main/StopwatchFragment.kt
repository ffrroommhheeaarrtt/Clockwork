package org.fromheart.clockwork.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.fromheart.clockwork.*
import org.fromheart.clockwork.adapter.StopwatchFlagAdapter
import org.fromheart.clockwork.databinding.FragmentStopwatchBinding
import org.fromheart.clockwork.repository.StopwatchRepository
import org.fromheart.clockwork.service.StopwatchService
import org.fromheart.clockwork.state.StopwatchState
import org.fromheart.clockwork.viewmodel.StopwatchViewModel
import org.fromheart.clockwork.viewmodel.StopwatchViewModelFactory

class StopwatchFragment: Fragment() {

    private val viewModel: StopwatchViewModel by viewModels {
        StopwatchViewModelFactory(StopwatchRepository(requireActivity().application.app.database.stopwatchDao()))
    }

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
            (stopwatchFlagRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            stopwatchFlagRecyclerView.adapter = adapter

            startFab.setOnClickListener { viewModel.start() }
            stopFab.setOnClickListener { viewModel.stop() }
            flagFab.setOnClickListener {
                ContextCompat.startForegroundService(
                    requireContext(),
                    Intent(requireContext(), StopwatchService::class.java).setAction(ACTION_SET_STOPWATCH_FLAG)
                )
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.stopwatchFlagFlow.collect {
                    adapter.submitList(it) {
                        stopwatchFlagRecyclerView.scrollToPosition(0)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    var stopwatchJob: Job? = null
                    viewModel.stopwatchFlow.collect { stopwatch ->
                        val serviceIntent = Intent(requireContext(), StopwatchService::class.java)
                        when (stopwatch.state) {
                            StopwatchState.STOPPED -> {
                                stopwatchJob?.cancel()
                                startFab.setImageResource(R.drawable.ic_play)
                                stopFab.visibility = View.INVISIBLE
                                flagFab.visibility = View.INVISIBLE
                                stopwatchTimeTextView.text = getText(R.string.stopwatch_time_text_view)
                            }
                            StopwatchState.PAUSED -> {
                                stopwatchJob?.cancel()
                                startFab.setImageResource(R.drawable.ic_play)
                                flagFab.visibility = View.INVISIBLE
                                stopwatchTimeTextView.text = getFormattedStopwatchTime(StopwatchRepository.stopwatchTimeFlow.first())
                            }
                            StopwatchState.STARTED -> {
                                ContextCompat.startForegroundService(requireContext(), serviceIntent)
                                startFab.setImageResource(R.drawable.ic_pause)
                                stopFab.visibility = View.VISIBLE
                                flagFab.visibility = View.VISIBLE
                                stopwatchJob = launch {
                                    StopwatchRepository.stopwatchTimeFlow.collect {
                                        stopwatchTimeTextView.text = getFormattedStopwatchTime(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}