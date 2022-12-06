package org.fromheart.clockwork.ui.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.TimeZoneModel
import org.fromheart.clockwork.databinding.FragmentClockBinding
import org.fromheart.clockwork.ui.adapter.ClockAdapter
import org.fromheart.clockwork.ui.viewmodel.ClockViewModel
import org.fromheart.clockwork.util.disableSimpleItemAnimator
import org.fromheart.clockwork.util.formatClockTime
import org.fromheart.clockwork.util.formatTimeZoneTime
import org.fromheart.clockwork.util.getFormattedClockDate
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.*

class ClockFragment : Fragment(), ClockAdapter.ClockListener {

    private val viewModel: ClockViewModel by activityViewModel()

    private lateinit var binding: FragmentClockBinding

    private val timeZoneMap = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finish() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentClockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ClockAdapter(this)

        binding.apply {
            clockRecycler.disableSimpleItemAnimator()
            clockRecycler.adapter = adapter
            adapter.clockTouchHelper.attachToRecyclerView(clockRecycler)

            clockFab.setOnClickListener {
                findNavController().navigate(ClockFragmentDirections.actionClockFragmentToTimeZoneFragment())
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.clockFlow.collect { adapter.submitList(it) }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.timeFlow.collect {
                        timeTextView.text = formatClockTime(it)
                        dateTextView.text = requireContext().getFormattedClockDate(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) {
                        timeZoneMap.forEach {
                            it.value.text = formatTimeZoneTime(TimeZone.getTimeZone(it.key))
                        }
                        delay(1000L)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        timeZoneMap.clear()
    }

    override fun onTimeTextViewBound(timeZone: TimeZoneModel, textView: TextView) {
        if (timeZone.id !in timeZoneMap) {
            textView.text = formatTimeZoneTime(TimeZone.getTimeZone(timeZone.id))
            timeZoneMap[timeZone.id] = textView
        }
    }

    override fun onSwiped(timeZone: TimeZoneModel) {
        timeZoneMap.remove(timeZone.id)
        viewModel.deleteClock(timeZone)
    }
}