package org.fromheart.clockwork.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.fromheart.clockwork.util.getTimerTime

@Entity(tableName = "timer")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val time: Long = getTimerTime(hour, minute, second),
    val state: TimerState = TimerState.STOPPED
)

enum class TimerState(val state: Int) {
    STOPPED(0),
    PAUSED(1),
    STARTED(2)
}