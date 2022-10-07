package org.fromheart.clockwork.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fromheart.clockwork.*
import org.fromheart.clockwork.data.AlarmDao
import org.fromheart.clockwork.receiver.AlarmReceiver
import org.fromheart.clockwork.ui.main.MainActivity
import java.util.*

class AlarmRepository(val alarmDao: AlarmDao) {

    suspend fun setAlarm(context: Context) = withContext(Dispatchers.Default) {
        if (!context.isScheduleExactAlarmPermissionAllowed()) return@withContext

        val alarmManager = context.getAlarmManager()
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            FLAG_IMMUTABLE
        )
        val receiverPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlarmReceiver::class.java),
            FLAG_IMMUTABLE
        )

        val alarms = alarmDao.getNextAlarms()
        if (alarms.isEmpty()) {
            alarmManager.cancel(receiverPendingIntent)
            //
            Log.d(TAG, "cancel")
            //
        } else {
            val alarmTime = alarms.first().time
            AlarmManagerCompat.setAlarmClock(alarmManager, alarmTime, showPendingIntent, receiverPendingIntent)
            //
            Calendar.getInstance().apply {
                timeInMillis = alarmTime
                Log.d(TAG, time.toString())
            }
            //
        }
    }

    suspend fun setNextAlarm(context: Context) = withContext(Dispatchers.IO) {
        alarmDao.getNextAlarms().forEach { alarm ->
            if (alarm.daysSet.isEmpty()) alarmDao.update(alarm.copy(status = false, daysLabel = ""))
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
                alarmDao.update(alarm.copy(time = alarmTime))
            }
        }
        setAlarm(context)
    }

    suspend fun updateTime(context: Context) = withContext(Dispatchers.IO) {
        alarmDao.getAlarmsForTimeChange().forEach { alarm ->
            alarmDao.update(
                alarm.copy(
                    time = getNextAlarmTime(alarm.hour, alarm.minute, alarm.daysSet),
                    daysLabel = if (alarm.daysSet.isEmpty()) context.getDaysLabel(alarm.hour, alarm.minute) else alarm.daysLabel
                )
            )
        }
        setAlarm(context)
    }
}
