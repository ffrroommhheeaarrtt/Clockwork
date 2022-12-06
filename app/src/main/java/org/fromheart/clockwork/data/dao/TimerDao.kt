package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.TimerEntity

@Dao
interface TimerDao {

    @Insert
    suspend fun insert(timer: TimerEntity)

    @Update
    suspend fun update(timer: TimerEntity)

    @Update
    suspend fun update(timerList: List<TimerEntity>)

    @Delete
    suspend fun delete(timer: TimerEntity)

    @Query("select * from timer where id == :id")
    suspend fun getTimer(id: Long): TimerEntity?

    @Query("select * from timer")
    suspend fun getTimers(): List<TimerEntity>

    @Query("select * from timer order by state desc, hour, minute, second")
    fun getTimerFlow(): Flow<List<TimerEntity>>
}