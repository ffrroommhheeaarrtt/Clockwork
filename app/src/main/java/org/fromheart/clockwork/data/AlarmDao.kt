package org.fromheart.clockwork.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert
    suspend fun insert(alarm: Alarm)

    @Update
    suspend fun update(vararg alarm: Alarm)

    @Update
    suspend fun update(alarm: List<Alarm>)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("select * from alarm where id == :id")
    suspend fun getAlarm(id: Long): Alarm

    @Query("select * from alarm where visibility limit 1")
    suspend fun getOpenAlarm(): Alarm?

    @Query("select id from alarm order by id desc limit 1")
    suspend fun getLastId(): Long

    @Query("select * from alarm order by hour, minute")
    fun getAlarms(): Flow<List<Alarm>>

    @Query("select * from alarm where status and time == (select min(time) from alarm where status)")
    suspend fun getNextAlarms(): List<Alarm>

    @Query("""select * from alarm where daysSet != "" or status""")
    suspend fun getAlarmsForTimeChange(): List<Alarm>

    @Query("""select * from alarm where daysSet == "" and status """)
    suspend fun getAlarmsForDayChange(): List<Alarm>
}