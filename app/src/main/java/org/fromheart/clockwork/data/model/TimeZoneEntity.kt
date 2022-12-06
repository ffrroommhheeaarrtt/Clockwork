package org.fromheart.clockwork.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_zone")
data class TimeZoneEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "zone_name")
    val zoneName: String,
    val added: Boolean = false
)