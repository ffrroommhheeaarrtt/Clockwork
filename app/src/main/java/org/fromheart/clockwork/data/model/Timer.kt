package org.fromheart.clockwork.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val time: Long,
    @ColumnInfo(name = "current_time")
    val currentTime: Long = time,
    val state: TimerState = TimerState.STOPPED
)

enum class TimerState(val state: Int) {
    STOPPED(0),
    PAUSED(1),
    STARTED(2)
}