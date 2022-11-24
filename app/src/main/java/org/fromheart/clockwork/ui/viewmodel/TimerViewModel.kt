package org.fromheart.clockwork.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.Timer
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.data.repository.TimerRepository
import org.fromheart.clockwork.util.getTimerTime

class TimerViewModel(private val repository: TimerRepository) : ViewModel() {

    val timerChannelMap = repository.timerChannelMap

    val timerFlow = repository.timerFlow

    val alertTimerTime = repository.alertTimerTime

    private val _pointer = MutableStateFlow(1)
    val pointer = _pointer.asStateFlow()

    private val _hour = MutableStateFlow(0)
    val hour = _hour.asStateFlow()

    private val _minute = MutableStateFlow(0)
    val minute = _minute.asStateFlow()

    private val _second = MutableStateFlow(0)
    val second = _second.asStateFlow()

    fun setPointer(position: Int) {
        _pointer.value = position
    }

    fun setTime(position: Int, time: Int) {
        when (position) {
            0 -> _hour.value = time
            1 -> _minute.value = time
            2 -> _second.value = time
        }
    }

    fun updateTimerKeyboard(timer: Timer) {
        _pointer.value = 1
        _hour.value = timer.hour
        _minute.value = timer.minute
        _second.value = timer.second
    }

    fun resetTimerKeyboard() {
        _pointer.value = 1
        _hour.value = 0
        _minute.value = 0
        _second.value = 0
    }

    fun addTimer() = viewModelScope.launch {
        repository.addTimer(Timer(hour = hour.value, minute = minute.value, second = second.value))
    }

    fun updateTimer(id: Long) = viewModelScope.launch {
        repository.getTimer(id)?.copy(
            hour = hour.value,
            minute = minute.value,
            second = second.value,
            time = getTimerTime(hour.value, minute.value, second.value)
        )?.let { repository.updateTimer(it) }
    }

    fun playButtonClicked(timer: Timer) = viewModelScope.launch {
        if (timer.state == TimerState.STARTED) repository.pauseTimer(timer) else repository.startTimer(timer)
    }

    fun stopTimer(timer: Timer) = viewModelScope.launch {
        repository.stopTimer(timer)
    }

    fun deleteTimer(timer: Timer) = viewModelScope.launch {
        repository.deleteTimer(timer)
    }
}

class TimerViewModelFactory(private val repository: TimerRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}