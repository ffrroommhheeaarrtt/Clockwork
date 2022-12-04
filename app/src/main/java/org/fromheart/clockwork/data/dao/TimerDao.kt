package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.TimerModel

@Dao
interface TimerDao {

    @Insert
    suspend fun insert(timer: TimerModel)

    @Update
    suspend fun update(timer: TimerModel)

    @Update
    suspend fun update(timerList: List<TimerModel>)

    @Delete
    suspend fun delete(timer: TimerModel)

    @Query("select * from timer where id == :id")
    suspend fun getTimer(id: Long): TimerModel?

    @Query("select * from timer")
    suspend fun getTimers(): List<TimerModel>

    @Query("select * from timer order by state desc, hour, minute, second")
    fun getTimerFlow(): Flow<List<TimerModel>>
}