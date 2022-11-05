package org.fromheart.clockwork.data.converter

import androidx.room.TypeConverter
import org.fromheart.clockwork.state.StopwatchState

object StopwatchConverter {

    @TypeConverter
    fun fromString(str: String): StopwatchState = StopwatchState.values().first { it.state == str }

    @TypeConverter
    fun fromState(state: StopwatchState): String = state.state
}