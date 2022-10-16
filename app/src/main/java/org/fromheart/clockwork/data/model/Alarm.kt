package org.fromheart.clockwork.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val time: Long,
    val status: Boolean = true,
    @ColumnInfo(name = "days_label")
    val daysLabel: String,
    val daysSet: Set<Int> = emptySet(),
    val visibility: Boolean = true
)