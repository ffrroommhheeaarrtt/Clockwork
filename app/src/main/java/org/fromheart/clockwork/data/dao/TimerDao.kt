package org.fromheart.clockwork.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.Timer
import org.fromheart.clockwork.data.model.TimerStatus

@Dao
interface TimerDao {

    @Insert
    suspend fun insert(timer: Timer)

    @Update
    suspend fun update(timer: Timer)

    @Delete
    suspend fun delete(timer: Timer)

    @Query("select * from timer where status == :statusStart")
    suspend fun getRunningTimer(statusStart: Int = TimerStatus.START.number): Timer?

    @Query("select * from timer order by status desc, hour, minute, second")
    fun getTimerFlow(): Flow<List<Timer>>
}