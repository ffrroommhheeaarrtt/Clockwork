package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.AlarmEntity

@Dao
interface AlarmDao {

    @Insert
    suspend fun insert(alarm: AlarmEntity)

    @Update
    suspend fun update(vararg alarms: AlarmEntity)

    @Update
    suspend fun update(alarmList: List<AlarmEntity>)

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("select * from alarm where id == :id")
    suspend fun getAlarm(id: Long): AlarmEntity?

    @Query("select * from alarm where open")
    suspend fun getOpenAlarm(): AlarmEntity?

    @Query("select * from alarm")
    suspend fun getAlarms(): List<AlarmEntity>

    @Query("select * from alarm order by hour, minute")
    fun getAlarmFlow(): Flow<List<AlarmEntity>>
}