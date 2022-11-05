package org.fromheart.clockwork.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.fromheart.clockwork.*
import org.fromheart.clockwork.data.dao.AlarmDao
import org.fromheart.clockwork.receiver.AlarmReceiver
import org.fromheart.clockwork.ui.main.MainActivity
import java.util.*

class AlarmRepository(val dao: AlarmDao) {

    suspend fun setAlarm(context: Context) = supervisorScope {
        if (!context.isScheduleExactAlarmPermissionAllowed()) return@supervisorScope

        val alarmManager = context.getAlarmManager()
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

        val alarms = dao.getNextAlarms()
        if (alarms.isEmpty()) {
            context.dataStore.edit { it[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)] = 0 }
            alarmManager.cancel(receiverPendingIntent)
            //
            Log.d(TAG, "alarm canceled")
            //
        } else {
            val alarmTime = alarms.first().time
            if (alarmTime != context.dataStore.data.first()[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)]) {
                context.dataStore.edit { it[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)] = alarmTime }
                AlarmManagerCompat.setAlarmClock(alarmManager, alarmTime, showPendingIntent!!, receiverPendingIntent)
            }
            //
            Calendar.getInstance().apply {
                timeInMillis = alarmTime
                Log.d(TAG, time.toString())
            }
            //
        }
    }

    suspend fun setNextAlarm(context: Context) = supervisorScope {
        dao.getNextAlarms().forEach { alarm ->
            if (alarm.daysSet.isEmpty()) dao.update(alarm.copy(status = false, daysLabel = ""))
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
                dao.update(alarm.copy(time = alarmTime))
            }
        }
        setAlarm(context)
    }

    suspend fun updateTime(context: Context) = supervisorScope {
        dao.getAlarmsForTimeChange().forEach { alarm ->
            dao.update(
                alarm.copy(
                    time = getNextAlarmTime(alarm.hour, alarm.minute, alarm.daysSet),
                    daysLabel = if (alarm.daysSet.isEmpty()) context.getDaysLabel(alarm.hour, alarm.minute) else alarm.daysLabel
                )
            )
        }
        setAlarm(context)
    }
}
