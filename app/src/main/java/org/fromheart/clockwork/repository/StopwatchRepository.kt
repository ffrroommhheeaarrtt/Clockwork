package org.fromheart.clockwork.repository

import org.fromheart.clockwork.data.dao.StopwatchDao
import org.fromheart.clockwork.data.model.Stopwatch
import org.fromheart.clockwork.state.StopwatchState

class StopwatchRepository(val dao: StopwatchDao) {

    suspend fun getTime(): Long = dao.getStopwatchTime().time

    suspend fun setState(state: StopwatchState) = dao.insert(Stopwatch(state = state))
}