package org.fromheart.clockwork.data.repository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.fromheart.clockwork.data.dao.StopwatchDao
import org.fromheart.clockwork.data.model.StopwatchModel
import org.fromheart.clockwork.data.model.StopwatchFlagModel
import org.fromheart.clockwork.data.model.StopwatchState

class StopwatchRepository (private val dao: StopwatchDao) {

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
                dao.insert(StopwatchFlagModel(id, time, time))
            }
            else {
                id = flag.id + 1L
                dao.insert(StopwatchFlagModel(id, time - flag.flagTime, time))
            }
            id
        }
    }

    suspend fun resetRunningStopwatch() {
        dao.insert(StopwatchModel())
        dao.deleteFlags()
    }

    suspend fun addDefaultStopwatch() {
        if (dao.isEmptyStopwatch() || dao.getStopwatch().state == StopwatchState.STARTED) resetRunningStopwatch()
    }
}