package org.fromheart.clockwork.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.fromheart.clockwork.state.StopwatchState

@Entity(tableName = "stopwatch")
data class Stopwatch(
    @PrimaryKey
    val id: Int = 0,
    val state: StopwatchState = StopwatchState.STOPPED,
)

@Entity(tableName = "stopwatch_time")
data class StopwatchTime(
    @PrimaryKey
    val id: Int = 0,
    val time: Long = 0L
)

@Entity(tableName = "stopwatch_flag")
data class StopwatchFlag(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "time_difference")
    val timeDifference: Long,
    @ColumnInfo(name = "flag_time")
    val flagTime: Long
)
