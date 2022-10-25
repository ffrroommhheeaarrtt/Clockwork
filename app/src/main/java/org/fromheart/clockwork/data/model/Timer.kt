package org.fromheart.clockwork.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.fromheart.clockwork.getTimerTime

@Entity(tableName = "timer")
data class Timer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val time: Long = getTimerTime(hour, minute, second),
    val status: Int = TimerStatus.STOP.number
)

enum class TimerStatus(val number: Int) {
    STOP(0),
    PAUSE(1),
    START(2)
}

