package org.fromheart.clockwork.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.fromheart.clockwork.*
import org.fromheart.clockwork.data.model.Alarm
import org.fromheart.clockwork.repository.AlarmRepository
import java.util.*

class AlarmViewModel(application: Application, private val repository: AlarmRepository) : AndroidViewModel(application) {

    private val context: Context
        get() = getApplication<App>().applicationContext

    private val alarmDao = repository.alarmDao

    private val date: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val alarmFlow = alarmDao.getAlarmFlow()

    val currentDayFlow = flow {
        var lastLoginDate = context.dataStore.data.first()[longPreferencesKey(PREFERENCES_KEY_LAST_LOGIN_DATE)] ?: date
        while (true) {
            val currentDate = date
            if (lastLoginDate != currentDate) {
                lastLoginDate = currentDate
                emit(true)
            } else emit(false)
            delay(1000L)
        }
    }

    fun addAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmDao.getOpenAlarm()?.let { alarmDao.update(it.copy(isOpened = false)) }
        alarmDao.insert(alarm)
        repository.setAlarm(context)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmDao.update(alarm)
    }

    fun updateAndSetAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmDao.update(alarm)
        repository.setAlarm(context)
    }

    fun updateAlarmDays() = viewModelScope.launch {
        context.dataStore.edit { it[longPreferencesKey(PREFERENCES_KEY_LAST_LOGIN_DATE)] = date }
        alarmDao.update(alarmDao.getAlarmsForDayChange().map {
            it.copy(daysLabel = context.getDaysLabel(it.hour, it.minute))
        })
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmDao.delete(alarm)
        if (alarm.status) repository.setAlarm(context)
    }

    fun itemClick(alarm: Alarm) = viewModelScope.launch {
        val openAlarm = alarmDao.getOpenAlarm()
        when (openAlarm?.id) {
            null -> alarmDao.update(alarm.copy(isOpened = true))
            alarm.id -> alarmDao.update(alarm.copy(isOpened = false))
            else -> alarmDao.update(alarm.copy(isOpened = true), openAlarm.copy(isOpened = false))
        }
    }
}

class AlarmViewModelFactory(private val application: Application, private val repository: AlarmRepository) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(application.app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}