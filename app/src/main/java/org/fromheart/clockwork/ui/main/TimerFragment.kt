package org.fromheart.clockwork.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import org.fromheart.clockwork.adapter.TimerAdapter
import org.fromheart.clockwork.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {

    private lateinit var binding: FragmentTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finish() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TimerAdapter()
        binding.apply {
            (timerRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            timerRecycler.adapter = adapter

            setTimerFab.setOnClickListener {
                findNavController().navigate(TimerFragmentDirections.actionTimerFragmentToTimerKeyboardFragment())
            }
        }
    }
}