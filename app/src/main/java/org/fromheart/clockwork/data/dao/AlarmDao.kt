package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.Alarm

@Dao
interface AlarmDao {

    @Insert
    suspend fun insert(alarm: Alarm)

    @Update
    suspend fun update(vararg alarms: Alarm)

    @Update
    suspend fun update(alarmList: List<Alarm>)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("select * from alarm where id == :id")
    suspend fun getAlarm(id: Long): Alarm?

    @Query("select * from alarm where open")
    suspend fun getOpenAlarm(): Alarm?

    @Query("select * from alarm")
    suspend fun getAlarms(): List<Alarm>

    @Query("select * from alarm order by hour, minute")
    fun getAlarmFlow(): Flow<List<Alarm>>
}