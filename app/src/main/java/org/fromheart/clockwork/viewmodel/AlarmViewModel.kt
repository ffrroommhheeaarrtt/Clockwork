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

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context
        get() = getApplication<App>().applicationContext

    private val repository = AlarmRepository(application.app.database.alarmDao())
    private val alarmDao = AlarmRepository(application.app.database.alarmDao()).alarmDao

    private val date: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private var previousAlarmId: Long? = null

    val allAlarms: Flow<List<Alarm>> = alarmDao.getAlarms()

    val currentDay: Flow<Boolean> = flow {
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

    fun addNewAlarm(alarm: Alarm) = viewModelScope.launch {
        if (previousAlarmId != null) {
            val previousAlarm = alarmDao.getAlarm(previousAlarmId!!)
            alarmDao.update(previousAlarm.copy(visibility = false))
        }
        alarmDao.insert(alarm)
        previousAlarmId = alarmDao.getLastId()
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
        when (previousAlarmId) {
            null -> {}
            alarm.id -> previousAlarmId = null
            else -> {
                val previousAlarm = alarmDao.getAlarm(previousAlarmId!!)
                alarmDao.update(previousAlarm.copy(visibility = false))
                previousAlarmId = null
            }
        }
        if (alarm.status) repository.setAlarm(context)
    }

    fun itemClick(alarm: Alarm) = viewModelScope.launch {
        val newAlarm: Alarm
        when (previousAlarmId) {
            null -> {
                newAlarm = alarm.copy(visibility = true)
                alarmDao.update(newAlarm)
                previousAlarmId = newAlarm.id
            }
            alarm.id -> {
                newAlarm = alarm.copy(visibility = false)
                alarmDao.update(newAlarm)
                previousAlarmId = null
            }
            else -> {
                newAlarm = alarm.copy(visibility = true)
                val previousAlarm = alarmDao.getAlarm(previousAlarmId!!)
                alarmDao.update(newAlarm, previousAlarm.copy(visibility = false))
                previousAlarmId = newAlarm.id
            }
        }
    }
}

class AlarmViewModelFactory(private val application: Application) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(application.app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}