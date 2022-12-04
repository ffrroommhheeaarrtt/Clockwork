package org.fromheart.clockwork.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.fromheart.clockwork.App
import org.fromheart.clockwork.data.model.AlarmModel
import org.fromheart.clockwork.data.repository.AlarmRepository
import org.fromheart.clockwork.util.PREFERENCES_KEY_LAST_LOGIN_DATE
import org.fromheart.clockwork.util.SECOND_IN_MILLIS
import org.fromheart.clockwork.util.dataStore
import org.fromheart.clockwork.util.getDaysLabel
import java.util.*

class AlarmViewModel(application: Application, private val repository: AlarmRepository) : AndroidViewModel(application) {

    private val context: Context
        get() = getApplication<App>().applicationContext

    private val date: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val alarmFlow = repository.alarmFlow

    val currentDayFlow = flow {
        var lastLoginDate = context.dataStore.data.first()[longPreferencesKey(PREFERENCES_KEY_LAST_LOGIN_DATE)] ?: date
        while (true) {
            val currentDate = date
            if (lastLoginDate != currentDate) {
                lastLoginDate = currentDate
                emit(true)
            } else emit(false)
            delay(SECOND_IN_MILLIS)
        }
    }

    fun addAlarm(alarm: AlarmModel) = viewModelScope.launch {
        repository.getOpenAlarm()?.let { repository.updateAlarm(it.copy(open = false)) }
        repository.addAlarm(alarm)
        repository.setAlarm(context)
    }

    fun updateAlarm(alarm: AlarmModel) = viewModelScope.launch {
        repository.updateAlarm(alarm)
    }

    fun updateAndSetAlarm(alarm: AlarmModel) = viewModelScope.launch {
        repository.updateAlarm(alarm)
        repository.setAlarm(context)
    }

    fun updateAlarmDays() = viewModelScope.launch {
        context.dataStore.edit { it[longPreferencesKey(PREFERENCES_KEY_LAST_LOGIN_DATE)] = date }
        repository.updateAlarm(repository.getAlarmsForDayChange().map {
            it.copy(daysLabel = context.getDaysLabel(it.hour, it.minute))
        })
    }

    fun deleteAlarm(alarm: AlarmModel) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
        if (alarm.status) repository.setAlarm(context)
    }

    fun setAlarm() = viewModelScope.launch {
        repository.setAlarm(context)
    }

    fun itemClicked(alarm: AlarmModel) = viewModelScope.launch {
        repository.getOpenAlarm().let { openAlarm ->
            when (openAlarm?.id) {
                null -> repository.updateAlarm(alarm.copy(open = true))
                alarm.id -> repository.updateAlarm(alarm.copy(open = false))
                else -> repository.updateAlarm(alarm.copy(open = true), openAlarm.copy(open = false))
            }
        }
    }
}