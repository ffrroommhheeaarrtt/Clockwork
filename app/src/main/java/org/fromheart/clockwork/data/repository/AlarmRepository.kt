package org.fromheart.clockwork.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import org.fromheart.clockwork.data.dao.AlarmDao
import org.fromheart.clockwork.data.model.AlarmModel
import org.fromheart.clockwork.receiver.AlarmReceiver
import org.fromheart.clockwork.ui.screen.main.MainActivity
import org.fromheart.clockwork.util.*
import java.util.*

class AlarmRepository (private val dao: AlarmDao) {

    val alarmFlow = dao.getAlarmFlow()

    private suspend fun getNextAlarms(): List<AlarmModel> {
        return dao.getAlarms().filter { it.status }.let { list ->
            list.filter { alarm ->
                alarm.time == list.minOf { it.time }
            }
        }
    }

    private suspend fun getAlarmsForTimeChange(): List<AlarmModel> {
        return dao.getAlarms().filter { it.daysSet.isNotEmpty() || it.status }
    }

    suspend fun addAlarm(alarm: AlarmModel) {
        dao.insert(alarm)
    }

    suspend fun updateAlarm(vararg alarms: AlarmModel) {
        dao.update(*alarms)
    }

    suspend fun updateAlarm(alarmList: List<AlarmModel>) {
        dao.update(alarmList)
    }

    suspend fun deleteAlarm(alarm: AlarmModel) {
        dao.delete(alarm)
    }

    suspend fun getOpenAlarm(): AlarmModel? {
        return dao.getOpenAlarm()
    }

    suspend fun getAlarmsForDayChange(): List<AlarmModel> {
        return dao.getAlarms().filter { it.daysSet.isEmpty() && it.status }
    }

    suspend fun setAlarm(context: Context) {
        if (!context.isScheduleExactAlarmPermissionAllowed()) return

        val showPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(Intent(context, MainActivity::class.java).apply {
                action = ACTION_ALARM_FRAGMENT
            })
            getPendingIntent(0, FLAG_IMMUTABLE)
        }
        val receiverPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlarmReceiver::class.java),
            FLAG_IMMUTABLE
        )

        getNextAlarms().let { alarms ->
            if (alarms.isEmpty()) {
                context.dataStore.edit { it[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)] = 0 }
                context.alarmManager.cancel(receiverPendingIntent)
            } else {
                val alarmTime = alarms.first().time
                context.dataStore.edit { it[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)] = alarmTime }
                AlarmManagerCompat.setAlarmClock(context.alarmManager, alarmTime, showPendingIntent!!, receiverPendingIntent)
            }
        }
    }

    suspend fun setNextAlarm(context: Context) {
        getNextAlarms().forEach { alarm ->
            if (alarm.daysSet.isEmpty()) updateAlarm(alarm.copy(status = false, daysLabel = ""))
            else {
                val alarmTime = Calendar.getInstance().apply {
                    timeInMillis = alarm.time
                    val today = dayOfWeek
                    val days = alarm.daysSet
                    val alarmDay = days.find { it > today } ?: days.first()
                    add(
                        Calendar.DAY_OF_YEAR,
                        when(alarmDay) {
                            today -> 7
                            in 0 until today -> 7 - today + alarmDay
                            else -> alarmDay - today
                        }
                    )
                }.timeInMillis
                updateAlarm(alarm.copy(time = alarmTime))
            }
        }
        setAlarm(context)
    }

    suspend fun updateTime(context: Context) {
        getAlarmsForTimeChange().map { alarm ->
            alarm.copy(
                time = getNextAlarmTime(alarm.hour, alarm.minute, alarm.daysSet),
                daysLabel = if (alarm.daysSet.isEmpty()) context.getDaysLabel(alarm.hour, alarm.minute) else alarm.daysLabel
            )
        }.let { updateAlarm(it) }
        setAlarm(context)
    }

    suspend fun closeAlarm() {
        getOpenAlarm()?.let {
            updateAlarm(it.copy(open = false))
        }
    }
}
