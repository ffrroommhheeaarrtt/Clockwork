package org.fromheart.clockwork.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class AlarmModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val time: Long,
    val status: Boolean = true,
    @ColumnInfo(name = "days_label")
    val daysLabel: String,
    @ColumnInfo(name = "days_set")
    val daysSet: Set<Int> = emptySet(),
    val open: Boolean = true
)