package org.fromheart.clockwork.data.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.fromheart.clockwork.data.dao.TimerDao
import org.fromheart.clockwork.data.model.Timer
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.util.getTimerTime

class TimerRepository private constructor(private val dao: TimerDao) {

    val timerChannelMap = mutableMapOf<Long, Channel<Long>>()

    val timerFlow = dao.getTimerFlow()

    val runningTimersChannel = Channel<Timer>(Channel.CONFLATED)

    private val _alertTimerTime = MutableStateFlow(0L)
    val alertTimerTime = _alertTimerTime.asStateFlow()

    fun setAlertTimerTime(time: Long) { _alertTimerTime.value = time }

    suspend fun getTimer(id: Long): Timer? {
        return dao.getTimer(id)
    }

    suspend fun addTimer(timer: Timer) {
        dao.insert(timer)
    }

    suspend fun updateTimer(timer: Timer) {
        dao.update(timer)
    }

    suspend fun deleteTimer(timer: Timer) {
        dao.delete(timer)
        if (timer.state == TimerState.STARTED) {
            runningTimersChannel.send(timer.copy(state = TimerState.STOPPED))
        }
    }

    suspend fun startTimer(timer: Timer) {
        runningTimersChannel.send(timer.copy(state = TimerState.STARTED))
    }

    suspend fun pauseTimer(timer: Timer) {
        runningTimersChannel.send(timer.copy(state = TimerState.PAUSED))
    }

    suspend fun pauseTimer(id: Long) {
        getTimer(id)?.let { pauseTimer(it) }
    }

    suspend fun stopTimer(timer: Timer) {
        runningTimersChannel.send(timer.copy(state = TimerState.STOPPED))
    }

    suspend fun stopTimer(id: Long) {
        getTimer(id)?.let { stopTimer(it) }
    }

    suspend fun resetUnfinishedTimers() {
        dao.getTimers().filter {
            it.state == TimerState.STARTED
        }.map {
            it.copy(state = TimerState.STOPPED, time = getTimerTime(it))
        }.let { dao.update(it) }
    }

    suspend fun resetRunningTimers() {
        resetUnfinishedTimers()
        timerChannelMap.forEach { it.value.close() }
    }

    companion object {
        @Volatile
        private var instance: TimerRepository? = null

        fun getInstance(dao: TimerDao): TimerRepository {
            return instance ?: synchronized(this) {
                TimerRepository(dao)
            }.also { instance = it }
        }
    }
}