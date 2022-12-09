package org.fromheart.clockwork.data.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.fromheart.clockwork.data.dao.TimerDao
import org.fromheart.clockwork.data.model.TimerEntity
import org.fromheart.clockwork.data.model.TimerState
import org.koin.core.component.KoinComponent

class TimerRepository (private val dao: TimerDao) : KoinComponent {

    val timerChannelMap = mutableMapOf<Long, Channel<Long>>()

    val timerFlow = dao.getTimerFlow()

    val runningTimersChannel = Channel<TimerEntity>(Channel.CONFLATED)

    private val _alertTimerTime = MutableStateFlow(0L)
    val alertTimerTime = _alertTimerTime.asStateFlow()

    fun setAlertTimerTime(time: Long) {
        _alertTimerTime.value = time
    }


    suspend fun getTimer(id: Long): TimerEntity? {
        return dao.getTimer(id)
    }

    suspend fun addTimer(timer: TimerEntity) {
        dao.insert(timer)
    }

    suspend fun updateTimer(timer: TimerEntity) {
        dao.update(timer)
    }

    suspend fun deleteTimer(timer: TimerEntity) {
        dao.delete(timer)
        if (timer.state == TimerState.STARTED) {
            runningTimersChannel.send(timer.copy(state = TimerState.STOPPED))
        }
    }

    suspend fun startTimer(timer: TimerEntity) {
        runningTimersChannel.send(timer.copy(state = TimerState.STARTED))
    }

    suspend fun pauseTimer(timer: TimerEntity) {
        runningTimersChannel.send(timer.copy(state = TimerState.PAUSED))
    }

    suspend fun pauseTimer(id: Long) {
        getTimer(id)?.let { pauseTimer(it) }
    }

    suspend fun stopTimer(timer: TimerEntity) {
        runningTimersChannel.send(timer.copy(state = TimerState.STOPPED))
    }

    suspend fun stopTimer(id: Long) {
        getTimer(id)?.let { stopTimer(it) }
    }

    suspend fun resetUnfinishedTimers() {
        dao.getTimers().filter {
            it.state == TimerState.STARTED
        }.map {
            it.copy(state = TimerState.STOPPED, currentTime = it.time)
        }.let { dao.update(it) }
    }

    suspend fun resetRunningTimers() {
        resetUnfinishedTimers()
        timerChannelMap.forEach { it.value.close() }
    }
}