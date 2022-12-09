package org.fromheart.clockwork.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.TimerEntity
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.data.repository.TimerRepository
import org.fromheart.clockwork.util.*

class TimerViewModel(private val repository: TimerRepository) : ViewModel() {

    val timerChannelMap = repository.timerChannelMap

    val timerFlow = repository.timerFlow

    val alertTimerTime = repository.alertTimerTime

    private val _timerTime = MutableStateFlow(formatTimerTime(0L))
    val timerTime = _timerTime.asStateFlow()

    private fun String.toMillis(): Long {
        val (hour, minute, second) = this.split(":").map { it.toLong() }
        val time = hour.hoursToMillis() + minute.minutesToMillis() + second.secondsToMillis()
        return if (time <= MAX_TIMER_TIME) {
            _timerTime.value = formatTimerTime(time)
            time
        } else {
            _timerTime.value = formatTimerTime(MAX_TIMER_TIME)
            MAX_TIMER_TIME
        }
    }

    fun setTimerTime(time: String) {
        _timerTime.value = time
    }

    fun updateTimerKeyboard(timer: TimerEntity) {
        _timerTime.value = formatTimerTime(timer.time)
    }

    fun resetTimerKeyboard() {
        _timerTime.value = formatTimerTime(0L)
    }

    fun addTimer() = viewModelScope.launch {
        repository.addTimer(TimerEntity(time = timerTime.value.toMillis()))
    }

    fun updateTimer(id: Long) = viewModelScope.launch {
        repository.getTimer(id)?.let { timer ->
            repository.updateTimer(timer.copy(
                time = timerTime.value.toMillis(),
                currentTime = timerTime.value.toMillis()
            ))
        }
    }

    fun playButtonClicked(timer: TimerEntity) = viewModelScope.launch {
        if (timer.state == TimerState.STARTED) repository.pauseTimer(timer) else repository.startTimer(timer)
    }

    fun stopTimer(timer: TimerEntity) = viewModelScope.launch {
        repository.stopTimer(timer)
    }

    fun deleteTimer(timer: TimerEntity) = viewModelScope.launch {
        repository.deleteTimer(timer)
    }
}