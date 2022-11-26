package org.fromheart.clockwork.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.StopwatchState
import org.fromheart.clockwork.data.repository.StopwatchRepository

class StopwatchViewModel(private val repository: StopwatchRepository) : ViewModel() {

    val timeReceiverFlow = repository.timeChannel.receiveAsFlow()

    val flagFlow = repository.flagFlow

    val pauseTimeFlow = repository.pauseTimeFlow

    val stopwatchState = repository.stopwatchState

    fun playButtonClicked() = viewModelScope.launch {
        repository.setState(if (stopwatchState.value == StopwatchState.STARTED) StopwatchState.PAUSED else StopwatchState.STARTED)
    }

    fun stopStopwatch() = viewModelScope.launch {
        repository.setState(StopwatchState.STOPPED)
    }
}

