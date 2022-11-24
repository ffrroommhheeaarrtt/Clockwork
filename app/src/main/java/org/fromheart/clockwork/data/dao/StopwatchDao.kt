package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.Stopwatch
import org.fromheart.clockwork.data.model.StopwatchFlag

@Dao
interface StopwatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stopwatch: Stopwatch)

    @Update
    suspend fun update(stopwatch: Stopwatch)

    @Query("select count(*) == 0 from stopwatch")
    suspend fun isEmptyStopwatch(): Boolean

    @Query("select * from stopwatch")
    suspend fun getStopwatch(): Stopwatch

    @Query("select time from stopwatch")
    fun getPauseTimeFlow(): Flow<Long>


    @Insert
    suspend fun insert(stopwatchFlag: StopwatchFlag)

    @Query("delete from stopwatch_flag")
    suspend fun deleteFlags()

    @Query("select * from stopwatch_flag order by id desc limit 1")
    suspend fun getLastFlag(): StopwatchFlag?

    @Query("select * from stopwatch_flag order by id desc")
    fun getStopwatchFlagFlow(): Flow<List<StopwatchFlag>>
}