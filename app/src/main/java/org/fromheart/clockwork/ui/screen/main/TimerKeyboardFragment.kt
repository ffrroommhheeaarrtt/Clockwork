package org.fromheart.clockwork.ui.screen.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.fromheart.clockwork.*
import org.fromheart.clockwork.databinding.FragmentTimerKeyboardBinding
import org.fromheart.clockwork.ui.viewmodel.TimerViewModel
import org.fromheart.clockwork.ui.viewmodel.TimerViewModelFactory
import org.fromheart.clockwork.util.app
import org.fromheart.clockwork.util.getFormattedTime
import org.fromheart.clockwork.util.isDarkTheme

class TimerKeyboardFragment : Fragment() {

    private val viewModel: TimerViewModel by activityViewModels {
        TimerViewModelFactory(requireActivity().application.app.timerRepository)
    }

    private val navArgs: TimerKeyboardFragmentArgs by navArgs()

    private lateinit var binging: FragmentTimerKeyboardBinding

    private lateinit var bottomNavigation: BottomNavigationView

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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)
        bottomNavigation.visibility = View.GONE

        binging.apply {
            val timeButtons = listOf(hourButton, minuteButton, secondButton)
            val timeIcons = listOf(hourIcon, minuteIcon, secondIcon)
            val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue)
            val defaultColor = ContextCompat.getColor(
                requireContext(),
                if (requireContext().isDarkTheme()) R.color.white else R.color.black
            )

            fun select(position: Int) {
                timeButtons[position].setTextColor(selectedColor)
                timeIcons[position].setTextColor(selectedColor)
            }
            fun deselect(position: Int) {
                timeButtons[position].setTextColor(defaultColor)
                timeIcons[position].setTextColor(defaultColor)
            }

            timeButtons.forEachIndexed { index, button ->
                if (index == viewModel.pointer.value) select(index) else deselect(index)
                button.setOnClickListener {
                    it as AppCompatButton
                    if (it.textColors.defaultColor != selectedColor) {
                        for (i in 0..2) {
                            if (timeButtons[i].id == button.id) {
                                select(i)
                                viewModel.setPointer(i)
                            } else deselect(i)
                        }
                    }
                }
            }

            val numberButtons = listOf(
                oneButton,
                twoButton,
                threeButton,
                fourButton,
                fiveButton,
                sixButton,
                sevenButton,
                eightButton,
                nineButton,
                zeroButton
            )

            hourButton.text = getFormattedTime(viewModel.hour.value)
            minuteButton.text = getFormattedTime(viewModel.minute.value)
            secondButton.text = getFormattedTime(viewModel.second.value)

            numberButtons.forEach { button ->
                button.setOnClickListener {
                    it as AppCompatButton
                    val position = viewModel.pointer.value
                    val timeButton = timeButtons[position]
                    timeButton.text = when {
                        timeButton.text == "00" -> "0${it.text}"
                        timeButton.text[0] == '0' -> {
                            if (position == 0 || timeButton.text[1].digitToInt() in 1..5)
                                "${timeButton.text[1]}${it.text}"
                            else
                                timeButton.text
                        }
                        else -> timeButton.text
                    }
                    viewModel.setTime(position, timeButton.text.toString().toInt())
                }
            }
            clearButton.setOnClickListener {
                val position = viewModel.pointer.value
                val timeButton = timeButtons[position]
                timeButton.text = "00"
                viewModel.setTime(position, timeButton.text.toString().toInt())
            }
            backspaceButton.setOnClickListener {
                val position = viewModel.pointer.value
                val timeButton = timeButtons[position]
                timeButton.text = if (timeButton.text[0] == '0') "00" else "0${timeButton.text[0]}"
                viewModel.setTime(position, timeButton.text.toString().toInt())
            }

            okButton.setOnClickListener {
                if (timeButtons.any { it.text != "00" }) {
                    if (navArgs.timerId == 0L) viewModel.addTimer() else viewModel.updateTimer(navArgs.timerId)
                    findNavController().navigateUp()
                }
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