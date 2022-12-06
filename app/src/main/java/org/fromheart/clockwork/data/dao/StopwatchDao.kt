package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.StopwatchFlagEntity
import org.fromheart.clockwork.data.model.StopwatchEntity

@Dao
interface StopwatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stopwatch: StopwatchEntity)

    @Update
    suspend fun update(stopwatch: StopwatchEntity)

    @Query("select count(*) == 0 from stopwatch")
    suspend fun isEmptyStopwatch(): Boolean

    @Query("select * from stopwatch")
    suspend fun getStopwatch(): StopwatchEntity

    @Query("select time from stopwatch")
    fun getPauseTimeFlow(): Flow<Long>


    @Insert
    suspend fun insert(stopwatchFlag: StopwatchFlagEntity)

    @Query("delete from stopwatch_flag")
    suspend fun deleteFlags()

    @Query("select * from stopwatch_flag order by id desc limit 1")
    suspend fun getLastFlag(): StopwatchFlagEntity?

    @Query("select * from stopwatch_flag order by id desc")
    fun getStopwatchFlagFlow(): Flow<List<StopwatchFlagEntity>>
}