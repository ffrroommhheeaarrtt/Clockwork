package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.StopwatchFlagModel
import org.fromheart.clockwork.data.model.StopwatchModel

@Dao
interface StopwatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stopwatch: StopwatchModel)

    @Update
    suspend fun update(stopwatch: StopwatchModel)

    @Query("select count(*) == 0 from stopwatch")
    suspend fun isEmptyStopwatch(): Boolean

    @Query("select * from stopwatch")
    suspend fun getStopwatch(): StopwatchModel

    @Query("select time from stopwatch")
    fun getPauseTimeFlow(): Flow<Long>


    @Insert
    suspend fun insert(stopwatchFlag: StopwatchFlagModel)

    @Query("delete from stopwatch_flag")
    suspend fun deleteFlags()

    @Query("select * from stopwatch_flag order by id desc limit 1")
    suspend fun getLastFlag(): StopwatchFlagModel?

    @Query("select * from stopwatch_flag order by id desc")
    fun getStopwatchFlagFlow(): Flow<List<StopwatchFlagModel>>
}