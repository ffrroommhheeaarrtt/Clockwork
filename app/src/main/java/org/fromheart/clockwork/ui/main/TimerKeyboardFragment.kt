package org.fromheart.clockwork.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.fromheart.clockwork.databinding.FragmentTimerKeyboardBinding

class TimerKeyboardFragment : Fragment() {

    private lateinit var binging: FragmentTimerKeyboardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binging = FragmentTimerKeyboardBinding.inflate(inflater, container, false)
        return binging.root
    }
}