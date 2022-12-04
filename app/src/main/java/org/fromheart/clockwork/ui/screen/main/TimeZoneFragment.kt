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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.model.TimeZoneModel
import org.fromheart.clockwork.databinding.FragmentTimeZoneBinding
import org.fromheart.clockwork.ui.adapter.TimeZoneAdapter
import org.fromheart.clockwork.ui.viewmodel.ClockViewModel
import org.fromheart.clockwork.util.disableSimpleItemAnimator
import org.fromheart.clockwork.util.formatTimeZoneTime
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.*

class TimeZoneFragment : Fragment(), TimeZoneAdapter.TimeZoneListener {

    private val viewModel: ClockViewModel by activityViewModel()

    private lateinit var binding: FragmentTimeZoneBinding

    private lateinit var bottomNavigation: BottomNavigationView

    private val timeZoneMap = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTimeZoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)
        bottomNavigation.visibility = View.GONE

        val adapter = TimeZoneAdapter(this)

        binding.apply {
            timeZoneRecycler.disableSimpleItemAnimator()
            timeZoneRecycler.adapter = adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timeZoneFlow.collect { adapter.submitList(it) }
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

    override fun onDestroyView() {
        super.onDestroyView()

        bottomNavigation.visibility = View.VISIBLE
    }

    override fun onItemClicked(timeZone: TimeZoneModel) {
        viewModel.addClock(timeZone)
        findNavController().navigateUp()
    }

    override fun onTimeTextViewBound(timeZone: TimeZoneModel, textView: TextView) {
        if (timeZone.id !in timeZoneMap) {
            textView.text = formatTimeZoneTime(TimeZone.getTimeZone(timeZone.id))
            timeZoneMap[timeZone.id] = textView
        }
    }
}