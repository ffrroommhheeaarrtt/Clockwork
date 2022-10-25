package org.fromheart.clockwork.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.App
import org.fromheart.clockwork.app
import org.fromheart.clockwork.data.model.Timer
import org.fromheart.clockwork.data.model.TimerStatus
import org.fromheart.clockwork.repository.TimerRepository
import org.fromheart.clockwork.service.TimerService

class TimerViewModel(application: Application, repository: TimerRepository) : AndroidViewModel(application) {

    companion object {
        private var timerToUpdate: Timer? = null
    }

    private val context: Context
        get() = getApplication<App>().applicationContext

    private val timerDao = repository.timerDao

    private val _pointerState = MutableStateFlow(1)
    val pointerState: StateFlow<Int> = _pointerState

    private val _hourState = MutableStateFlow("00")
    val hourState: StateFlow<String> = _hourState

    private val _minuteState = MutableStateFlow("00")
    val minuteState: StateFlow<String> = _minuteState

    private val _secondState = MutableStateFlow("00")
    val secondState: StateFlow<String> = _secondState

    val timerFlow = timerDao.getTimerFlow()

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

    fun getTimerToUpdate(): Timer? = timerToUpdate
    fun setTimerToUpdate(timer: Timer?) { timerToUpdate = timer }

    fun addTimer(timer: Timer) = viewModelScope.launch {
        timerDao.insert(timer)
    }

    fun updateTimer(timer: Timer) = viewModelScope.launch {
        timerDao.update(timer)
    }

    fun changeTimerState(timer: Timer) = viewModelScope.launch {
        val runningTimer = timerDao.getRunningTimer()
        when {
            timer.status == TimerStatus.START.number -> {
                runningTimer?.let {
                    timerDao.update(it.copy(status = TimerStatus.PAUSE.number))
                    TimerService.pause()
                }
                timerDao.update(timer)
                ContextCompat.startForegroundService(context, Intent(context, TimerService::class.java))
            }
            timer.status == TimerStatus.PAUSE.number -> {
                TimerService.pause()
                timerDao.update(timer)
            }
            timer.status == TimerStatus.STOP.number && timer.id == runningTimer?.id -> {
                TimerService.stop()
                timerDao.update(timer)
            }
            else -> timerDao.update(timer)
        }
    }

    fun deleteTimer(timer: Timer) = viewModelScope.launch {
        if (timer.status == TimerStatus.START.number) TimerService.stop()
        timerDao.delete(timer)
    }
}

class TimerViewModelFactory(private val application: Application, private val repository: TimerRepository)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(application.app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}