package org.fromheart.clockwork.util

import android.util.Log
import org.fromheart.clockwork.data.model.TimerModel
import java.util.*
import kotlin.math.abs

private const val DEBUG_TAG = "clockwork_debug_tag"

val dayOfWeek: Int
    get() = Calendar.getInstance()[Calendar.DAY_OF_WEEK].let {
        return when(it) {
            0 -> 5
            1 -> 6
            else -> it - 2
        }
    }

fun log(msg: CharSequence) = Log.d(DEBUG_TAG, msg.toString())

fun formatTime(time: Int): String = "%02d".format(time)

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

fun formatTimerTime(hour: Int, minute: Int, second: Int): String {
    return if (hour != 0)
        "%d:%02d:%02d".format(hour, minute, second)
    else
        "%02d:%02d".format(minute, second)
}

fun formatTimerTime(time: Long): String {
    return formatTimerTime(
        (time / HOUR_IN_MILLIS).toInt(),
        (time % HOUR_IN_MILLIS / MINUTE_IN_MILLIS).toInt(),
        (time % HOUR_IN_MILLIS % MINUTE_IN_MILLIS / SECOND_IN_MILLIS).toInt()
    )
}

fun getTimerTime(hour: Int, minute: Int, second: Int): Long {
    return hour.hoursToMillis() + minute.minutesToMillis() + second.secondsToMillis()
}

fun getTimerTime(timer: TimerModel): Long {
    return timer.hour.hoursToMillis() + timer.minute.minutesToMillis() + timer.second.secondsToMillis()
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