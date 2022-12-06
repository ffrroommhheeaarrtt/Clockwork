package org.fromheart.clockwork.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fromheart.clockwork.data.model.TimeZoneEntity

@Dao
interface TimeZoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeZoneList: List<TimeZoneEntity>)

    @Update
    suspend fun update(timeZone: TimeZoneEntity)

    @Query("select count(*) == 0 from time_zone")
    suspend fun isEmpty(): Boolean

    @Query("select * from time_zone")
    suspend fun getTimeZoneList(): List<TimeZoneEntity>

    @Query("select * from time_zone where added order by zone_name")
    fun getClockFlow(): Flow<List<TimeZoneEntity>>
}