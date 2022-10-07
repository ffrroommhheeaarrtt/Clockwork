package org.fromheart.clockwork

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

fun getTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

val time: String
    get() = Calendar.getInstance().let { getTime(it[Calendar.HOUR_OF_DAY], it[Calendar.MINUTE]) }

val currentTime: Flow<String>
    get() = flow {
        while (true) {
            emit(time)
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

val date: Long
    get() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

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








