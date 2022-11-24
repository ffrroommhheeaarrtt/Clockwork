package org.fromheart.clockwork.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.data.repository.AlarmRepository

class MainViewModel(database: AppDatabase) : ViewModel() {

    private val alarmRepository = AlarmRepository.getInstance(database.alarmDao())

    fun setAlarm(context: Context) = viewModelScope.launch {
        alarmRepository.setAlarm(context)
    }
}

class MainViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}