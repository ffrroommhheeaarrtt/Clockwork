package org.fromheart.clockwork.ui.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.fromheart.clockwork.R
import org.fromheart.clockwork.databinding.FragmentTimerKeyboardBinding
import org.fromheart.clockwork.ui.viewmodel.TimerViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class TimerKeyboardFragment : Fragment() {

    private val viewModel: TimerViewModel by activityViewModel()

    private val navArgs: TimerKeyboardFragmentArgs by navArgs()

    private lateinit var binging: FragmentTimerKeyboardBinding

    private lateinit var bottomNavigation: BottomNavigationView

    private val timeList = mutableListOf<Char>()

    private fun setTime() = binging.apply {
        val timeArray = Array(6) { '0' }
        for (i in timeList.lastIndex downTo 0) {
            timeArray[timeArray.lastIndex - i] = timeList.reversed()[i]
        }
        val time = "${timeArray[0]}${timeArray[1]}:${timeArray[2]}${timeArray[3]}:${timeArray[4]}${timeArray[5]}"
        timerTimeTextview.text = time
        viewModel.setTimerTime(time)
        okButton.visibility = if (timeList.isEmpty()) View.INVISIBLE else View.VISIBLE
    }

    private fun updateTimerList() {
        viewModel.timerTime.value.filter { it != ':' }.toList().let { list ->
            for (i in list.indices) {
                if (list[i] != '0') {
                    timeList.addAll(list.subList(i, list.size))
                    break
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binging = FragmentTimerKeyboardBinding.inflate(inflater, container, false)
        return binging.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)
        bottomNavigation.visibility = View.GONE

        binging.apply {
            timerTimeTextview.text = viewModel.timerTime.value

            updateTimerList()

            okButton.visibility = if (timeList.isEmpty()) View.INVISIBLE else View.VISIBLE

            listOf(
                oneButton,
                twoButton,
                threeButton,
                fourButton,
                fiveButton,
                sixButton,
                sevenButton,
                eightButton,
                nineButton
            ).forEach { button ->
                button.setOnClickListener {
                    if (timeList.size < 6) {
                        timeList.add(button.text.first())
                        setTime()
                    }
                }
            }
            zeroButton.setOnClickListener {
                if (timeList.size in 1..5) {
                    timeList.add('0')
                    setTime()
                }
            }
            twoZerosButton.setOnClickListener {
                when (timeList.size) {
                    in 1..4 -> {
                        timeList.addAll(listOf('0', '0'))
                        setTime()
                    }
                    5 -> {
                        timeList.add('0')
                        setTime()
                    }
                }
            }
            backspaceButton.setOnClickListener {
                timeList.removeLastOrNull()
                setTime()
            }
            backspaceButton.setOnLongClickListener {
                timeList.clear()
                setTime()
                return@setOnLongClickListener true
            }

            okButton.setOnClickListener {
                if (navArgs.timerId == 0L) viewModel.addTimer() else viewModel.updateTimer(navArgs.timerId)
                findNavController().navigateUp()
            }
            cancelButton.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        bottomNavigation.visibility = View.VISIBLE
    }
}