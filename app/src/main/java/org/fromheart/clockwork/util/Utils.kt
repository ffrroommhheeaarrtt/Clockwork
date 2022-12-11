package org.fromheart.clockwork.util

import android.app.PendingIntent
import android.os.Build
import android.util.Log
import java.util.*
import kotlin.math.abs

private const val DEBUG_TAG = "clockwork_tag"

val FLAG_IMMUTABLE: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

val dayOfWeek: Int
    get() = Calendar.getInstance()[Calendar.DAY_OF_WEEK].let {
        return when(it) {
            0 -> 5
            1 -> 6
            else -> it - 2
        }
    }

fun log(msg: String) = Log.d(DEBUG_TAG, msg)

fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

fun getAlarmTime(hour: Int, minute: Int, transferToNextDay: Boolean = true): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    if (transferToNextDay) {
        if (System.currentTimeMillis() >= timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }
}.timeInMillis

fun getNextAlarmTime(hour: Int, minute: Int, days: Set<Int>): Long {
    return if (days.isEmpty()) getAlarmTime(hour, minute)
    else {
        Calendar.getInstance().apply {
            timeInMillis = getAlarmTime(hour, minute, false)
            val today = dayOfWeek
            if (!(days.contains(today) && System.currentTimeMillis() < timeInMillis)) {
                val alarmDay = days.find { it > today } ?: days.first()
                add(
                    Calendar.DAY_OF_YEAR,
                    when(alarmDay) {
                        today -> 7
                        in 0 until today -> 7 - today + alarmDay
                        else -> alarmDay - today
                    }
                )
            }
        }.timeInMillis
    }
}

fun formatTimerItemTime(time: Long): String {
    return if (time >= HOUR_IN_MILLIS) "%d:%02d:%02d".format(time.millisToHours(), time.millisToMinutes(), time.millisToSeconds())
    else "%02d:%02d".format(time.millisToMinutes(), time.millisToSeconds())
}

fun formatTimerTime(time: Long): String {
    return "%02d:%02d:%02d".format(time.millisToHours(), time.millisToMinutes(), time.millisToSeconds())
}

fun formatStopwatchTime(time: Long): String {
    return if (time < MINUTE_IN_MILLIS) "%02d.%02d".format(time / SECOND_IN_MILLIS, time % SECOND_IN_MILLIS / 10L)
    else "%d:%02d.%02d".format(
        time / MINUTE_IN_MILLIS,
        time % MINUTE_IN_MILLIS / SECOND_IN_MILLIS,
        time % MINUTE_IN_MILLIS % SECOND_IN_MILLIS / 10L
    )
}

fun formatStopwatchServiceTime(time: Long): String {
    return "%02d:%02d".format(time / MINUTE_IN_MILLIS, time % MINUTE_IN_MILLIS / SECOND_IN_MILLIS)
}

fun formatClockTime(time: Long): String = Calendar.getInstance().run {
    timeInMillis = time
    "%02d:%02d:%02d".format(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND))
}

fun formatTimeZoneTime(timeZone: TimeZone): String = Calendar.getInstance(timeZone).run {
    formatTime(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
}

fun formatTimeZoneDifference(timeZone: TimeZone): String {
    return (timeZone.getOffset(System.currentTimeMillis()) - TimeZone.getDefault().getOffset(System.currentTimeMillis())).let { offset ->
        if (offset % HOUR_IN_MILLIS == 0L) "%+dh".format(offset / HOUR_IN_MILLIS)
        else "%+dh %dm".format(offset / HOUR_IN_MILLIS, abs(offset % HOUR_IN_MILLIS / MINUTE_IN_MILLIS))
    }
}