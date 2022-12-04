package org.fromheart.clockwork.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.TimeZoneModel
import org.fromheart.clockwork.data.repository.ClockRepository

class ClockViewModel(private val repository: ClockRepository) : ViewModel() {

    val timeZoneFlow = repository.timeZoneFlow

    val clockFlow = repository.clockFlow

    val timeFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(100L)
        }
    }

    fun addClock(timeZone: TimeZoneModel) = viewModelScope.launch {
        repository.addClock(timeZone)
    }

    fun deleteClock(timeZone: TimeZoneModel) = viewModelScope.launch {
        repository.deleteClock(timeZone)
    }
}