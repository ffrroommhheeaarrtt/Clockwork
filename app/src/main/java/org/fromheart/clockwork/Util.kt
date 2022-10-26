package org.fromheart.clockwork

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

private const val HOUR_IN_MILLIS = 3600000L
private const val MINUTE_IN_MILLIS = 60000L
private const val SECOND_IN_MILLIS = 1000L

fun getFormattedTime(time: Int): String = "%02d".format(time)

fun getFormattedTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

fun getFormattedTime(hour: Int, minute: Int, second: Int): String {
    return if (hour != 0)
        "%d:%02d:%02d".format(hour, minute, second)
    else
        "%02d:%02d".format(minute, second)
}

val currentTime: String
    get() = Calendar.getInstance().let { getFormattedTime(it[Calendar.HOUR_OF_DAY], it[Calendar.MINUTE]) }

val currentTimeFlow: Flow<String>
    get() = flow {
        while (true) {
            emit(currentTime)
            delay(1000L)
        }
    }

val dayOfWeek: Int
    get() = Calendar.getInstance()[Calendar.DAY_OF_WEEK].let {
        return when(it) {
            0 -> 5
            1 -> 6
            else -> it - 2
        }
    }

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

fun getTimerTime(hour: Int, minute: Int, second: Int): Long {
    return hour * HOUR_IN_MILLIS + minute * MINUTE_IN_MILLIS + second * SECOND_IN_MILLIS
}

fun getFormattedTimerTime(time: Long): String {
    return getFormattedTime(
        (time / HOUR_IN_MILLIS).toInt(),
        (time % HOUR_IN_MILLIS / MINUTE_IN_MILLIS).toInt(),
        (time % HOUR_IN_MILLIS % MINUTE_IN_MILLIS / SECOND_IN_MILLIS).toInt()
    )
}







