package org.fromheart.clockwork.data.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.fromheart.clockwork.data.dao.StopwatchDao
import org.fromheart.clockwork.data.model.Stopwatch
import org.fromheart.clockwork.data.model.StopwatchFlag
import org.fromheart.clockwork.data.model.StopwatchState

class StopwatchRepository private constructor(private val dao: StopwatchDao) {

    val timeChannel = Channel<Long>(Channel.CONFLATED)

    val pauseTimeFlow = dao.getPauseTimeFlow()

    val flagFlow = dao.getStopwatchFlagFlow()

    private val _stopwatchState = MutableStateFlow(StopwatchState.STOPPED)
    val stopwatchState = _stopwatchState.asStateFlow()

    suspend fun getPauseTime(): Long = dao.getStopwatch().time

    suspend fun getLastFlagId(): Long? = dao.getLastFlag()?.id

    suspend fun setState(state: StopwatchState) {
        dao.update(dao.getStopwatch().copy(state = state))
        _stopwatchState.value = state
    }

    suspend fun setTime(time: Long) {
        dao.update(dao.getStopwatch().copy(time = time))
    }

    suspend fun setFlag(time: Long): Long {
        val id: Long
        return dao.getLastFlag().let { flag ->
            if (flag == null) {
                id = 1L
                dao.insert(StopwatchFlag(id, time, time))
            }
            else {
                id = flag.id + 1L
                dao.insert(StopwatchFlag(id, time - flag.flagTime, time))
            }
            id
        }
    }

    suspend fun resetRunningStopwatch() {
        dao.insert(Stopwatch())
        dao.deleteFlags()
    }

    suspend fun addDefaultStopwatch() {
        if (dao.isEmptyStopwatch() || dao.getStopwatch().state == StopwatchState.STARTED) resetRunningStopwatch()
    }

    companion object {
        @Volatile
        private var instance: StopwatchRepository? = null

        fun getInstance(dao: StopwatchDao): StopwatchRepository {
            return instance ?: synchronized(this) {
                StopwatchRepository(dao)
            }.also { instance = it }
        }
    }
}