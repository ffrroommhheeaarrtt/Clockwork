package org.fromheart.clockwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerKeyboardViewModel : ViewModel() {

    private val _pointerState = MutableStateFlow(1)
    val pointerState: StateFlow<Int> = _pointerState

    private val _hourState = MutableStateFlow("00")
    val hourState: StateFlow<String> = _hourState

    private val _minuteState = MutableStateFlow("00")
    val minuteState: StateFlow<String> = _minuteState

    private val _secondState = MutableStateFlow("00")
    val secondState: StateFlow<String> = _secondState

    private fun setHour(time: String) { _hourState.value = time }
    private fun setMinute(time: String) { _minuteState.value = time }
    private fun setSecond(time: String) { _secondState.value = time }

    fun setPointer(position: Int) { _pointerState.value = position }

    fun setTime(position: Int, time: String) {
        when (position) {
            0 -> setHour(time)
            1 -> setMinute(time)
            2 -> setSecond(time)
        }
    }
}

class TimerKeyboardViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerKeyboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerKeyboardViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}