package org.fromheart.clockwork.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_zone")
data class TimeZoneModel(
    @PrimaryKey
    val id: String,
    val zone: String,
    val added: Boolean = false
)