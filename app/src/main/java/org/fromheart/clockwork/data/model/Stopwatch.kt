package org.fromheart.clockwork.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stopwatch")
data class Stopwatch(
    @PrimaryKey
    val id: Int = 0,
    val state: StopwatchState = StopwatchState.STOPPED,
    val time: Long = 0L
)

enum class StopwatchState(val state: String) {
    STOPPED("stopped"),
    PAUSED("paused"),
    STARTED("started")
}

@Entity(tableName = "stopwatch_flag")
data class StopwatchFlag(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "time_difference")
    val timeDifference: Long,
    @ColumnInfo(name = "flag_time")
    val flagTime: Long
)

