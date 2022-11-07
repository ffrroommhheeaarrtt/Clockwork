package org.fromheart.clockwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.fromheart.clockwork.repository.StopwatchRepository
import org.fromheart.clockwork.state.StopwatchState

class StopwatchViewModel(private val repository: StopwatchRepository) : ViewModel() {

    private val dao = repository.dao

    val stopwatchFlow = dao.getStopWatchFlow()

    val stopwatchFlagFlow = dao.getStopwatchFlagFlow()

    suspend fun getTime(): Long = repository.getTime()

    fun start() = viewModelScope.launch {
        repository.setState(if (dao.getStopwatch().state == StopwatchState.STARTED) StopwatchState.PAUSED else StopwatchState.STARTED)
    }

    fun stop() = viewModelScope.launch {
        repository.setState(StopwatchState.STOPPED)
    }
}

class StopwatchViewModelFactory(private val repository: StopwatchRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopwatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StopwatchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

