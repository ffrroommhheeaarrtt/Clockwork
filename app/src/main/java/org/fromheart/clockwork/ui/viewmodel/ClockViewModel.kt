package org.fromheart.clockwork.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.model.TimeZoneEntity
import org.fromheart.clockwork.data.repository.ClockRepository

class ClockViewModel(private val repository: ClockRepository) : ViewModel() {

    val clockFlow = repository.clockFlow

    val timeFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(100L)
        }
    }

    private val _timeZoneList = MutableStateFlow(emptyList<TimeZoneEntity>())
    val timeZoneList = _timeZoneList.asStateFlow()

    init {
        setDefaultTimeZoneList()
    }

    fun setDefaultTimeZoneList() = viewModelScope.launch {
        _timeZoneList.value = repository.getTimeZoneList()
    }

    fun searchTimeZone(startWith: String) = viewModelScope.launch {
        startWith.trim().let {
            if (it.isEmpty()) _timeZoneList.value = repository.getTimeZoneList()
            else _timeZoneList.value = repository.getTimeZoneList(it)
        }
    }

    fun addClock(timeZone: TimeZoneEntity) = viewModelScope.launch {
        repository.addClock(timeZone)
    }

    fun deleteClock(timeZone: TimeZoneEntity) = viewModelScope.launch {
        repository.deleteClock(timeZone)
    }
}