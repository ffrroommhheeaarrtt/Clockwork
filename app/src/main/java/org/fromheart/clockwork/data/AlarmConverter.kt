package org.fromheart.clockwork.data

import androidx.room.TypeConverter

object AlarmConverter {

    @TypeConverter
    fun fromString(str: String): Set<Int> = if (str.isEmpty()) emptySet() else str.map { it.digitToInt() }.toSet()

    @TypeConverter
    fun fromSet(days: Set<Int>): String = days.joinToString("")
}