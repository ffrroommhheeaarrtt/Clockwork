package org.fromheart.clockwork.data.repository

import org.fromheart.clockwork.data.dao.TimeZoneDao
import org.fromheart.clockwork.data.model.TimeZoneEntity

class ClockRepository(private val dao: TimeZoneDao) {

    val clockFlow = dao.getClockFlow()

    suspend fun getTimeZoneList(): List<TimeZoneEntity> {
        return dao.getTimeZoneList().sortedBy { it.zoneName }
    }

    suspend fun getTimeZoneList(startWith: String): List<TimeZoneEntity> {
        return dao.getTimeZoneList().filter {
            it.zoneName.length >= startWith.length && it.zoneName.substring(0, startWith.length).equals(startWith, true)
        }.sortedBy { it.zoneName }
    }

    suspend fun addTimeZoneList(list: List<String>) {
        if (dao.isEmpty()) {
            dao.insert(
                list.map {
                    val (id, zone) = it.split("=")
                    TimeZoneEntity(id = id, zoneName = zone)
                }
            )
        }
    }

    suspend fun addClock(timeZone: TimeZoneEntity) {
        dao.update(timeZone.copy(added = true))
    }

    suspend fun deleteClock(timeZone: TimeZoneEntity) {
        dao.update(timeZone.copy(added = false))
    }
}