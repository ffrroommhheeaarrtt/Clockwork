package org.fromheart.clockwork.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.fromheart.clockwork.R
import org.fromheart.clockwork.databinding.FragmentTimerKeyboardBinding
import org.fromheart.clockwork.isDarkTheme
import org.fromheart.clockwork.viewmodel.TimerKeyboardViewModel
import org.fromheart.clockwork.viewmodel.TimerKeyboardViewModelFactory

class TimerKeyboardFragment : Fragment() {

    private val viewModel: TimerKeyboardViewModel by viewModels { TimerKeyboardViewModelFactory() }

    private lateinit var binging: FragmentTimerKeyboardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binging = FragmentTimerKeyboardBinding.inflate(inflater, container, false)
        return binging.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            fun unselect(position: Int) {
                timeButtons[position].setTextColor(defaultColor)
                timeIcons[position].setTextColor(defaultColor)
            }
            timeButtons.forEachIndexed { index, button ->
                if (index == viewModel.pointerState.value) select(index) else unselect(index)
                button.setOnClickListener {
                    it as AppCompatButton
                    if (it.textColors.defaultColor != selectedColor) {
                        for (i in 0..2) {
                            if (timeButtons[i].id == button.id) {
                                select(i)
                                viewModel.setPointer(i)
                            } else unselect(i)
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
            hourButton.text = viewModel.hourState.value
            minuteButton.text = viewModel.minuteState.value
            secondButton.text = viewModel.secondState.value
            numberButtons.forEach { button ->
                button.setOnClickListener {
                    it as AppCompatButton
                    val position = viewModel.pointerState.value
                    val timeButton = timeButtons[position]
                    timeButton.text = when {
                        timeButton.text == "00" -> "0${it.text}"
                        timeButton.text.first() == '0' -> {
                            if (position == 0 || timeButton.text[1].digitToInt() in 1..5)
                                "${timeButton.text[1]}${it.text}"
                            else
                                timeButton.text
                        }
                        else -> timeButton.text
                    }
                    viewModel.setTime(position, timeButton.text.toString())
                }
            }
            clearButton.setOnClickListener {
                val position = viewModel.pointerState.value
                val timeButton = timeButtons[position]
                timeButton.text = "00"
                viewModel.setTime(position, timeButton.text.toString())
            }
            backspaceButton.setOnClickListener {
                val position = viewModel.pointerState.value
                val timeButton = timeButtons[position]
                timeButton.text = if (timeButton.text.first() == '0') "00" else "0${timeButton.text.first()}"
                viewModel.setTime(position, timeButton.text.toString())
            }

            okButton.setOnClickListener {
                findNavController().navigate(TimerKeyboardFragmentDirections.actionTimerKeyboardFragmentToTimerFragment())
            }
            cancelButton.setOnClickListener {
                findNavController().navigate(TimerKeyboardFragmentDirections.actionTimerKeyboardFragmentToTimerFragment())
            }
        }
    }
}