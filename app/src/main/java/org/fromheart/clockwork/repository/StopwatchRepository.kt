package org.fromheart.clockwork.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import org.fromheart.clockwork.App
import org.fromheart.clockwork.data.dao.StopwatchDao
import org.fromheart.clockwork.data.model.Stopwatch
import org.fromheart.clockwork.state.StopwatchState
import kotlin.system.measureTimeMillis

class StopwatchRepository(val dao: StopwatchDao) {

    companion object {

        val stopwatchTimeFlow = flow {
            var time = App().database.stopwatchDao().getStopwatchTime().time
            while (true) {
                measureTimeMillis {
                    emit(time)
                    delay(10L)
                }.let { time += it }
            }
        }.shareIn(CoroutineScope(SupervisorJob()), SharingStarted.WhileSubscribed())
    }

    suspend fun setState(state: StopwatchState) = dao.insert(Stopwatch(state = state))
}