package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.AlarmModel

@Dao
interface AlarmDao {

    @Insert
    suspend fun insert(alarm: AlarmModel)

    @Update
    suspend fun update(vararg alarms: AlarmModel)

    @Update
    suspend fun update(alarmList: List<AlarmModel>)

    @Delete
    suspend fun delete(alarm: AlarmModel)

    @Query("select * from alarm where id == :id")
    suspend fun getAlarm(id: Long): AlarmModel?

    @Query("select * from alarm where open")
    suspend fun getOpenAlarm(): AlarmModel?

    @Query("select * from alarm")
    suspend fun getAlarms(): List<AlarmModel>

    @Query("select * from alarm order by hour, minute")
    fun getAlarmFlow(): Flow<List<AlarmModel>>
}