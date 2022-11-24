package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.Timer

@Dao
interface TimerDao {

    @Insert
    suspend fun insert(timer: Timer)

    @Update
    suspend fun update(timer: Timer)

    @Update
    suspend fun update(timerList: List<Timer>)

    @Delete
    suspend fun delete(timer: Timer)

    @Query("select * from timer where id == :id")
    suspend fun getTimer(id: Long): Timer?

    @Query("select * from timer")
    suspend fun getTimers(): List<Timer>

    @Query("select * from timer order by state desc, hour, minute, second")
    fun getTimerFlow(): Flow<List<Timer>>
}