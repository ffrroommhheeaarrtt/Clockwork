package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.TimeZoneModel

@Dao
interface TimeZoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeZoneList: List<TimeZoneModel>)

    @Update
    suspend fun update(timeZone: TimeZoneModel)

    @Query("select count(*) == 0 from time_zone")
    suspend fun isEmpty(): Boolean

    @Query("select * from time_zone order by zone")
    fun getTimeZoneFlow(): Flow<List<TimeZoneModel>>

    @Query("select * from time_zone where added order by zone")
    fun getClockFlow(): Flow<List<TimeZoneModel>>
}