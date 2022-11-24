package org.fromheart.clockwork.data.converter

import androidx.room.TypeConverter
import org.fromheart.clockwork.data.model.TimerState

object TimerConverter {

    @TypeConverter
    fun fromInt(num: Int): TimerState = TimerState.values().first { num == it.state }

    @TypeConverter
    fun fromState(state: TimerState): Int = state.state
}