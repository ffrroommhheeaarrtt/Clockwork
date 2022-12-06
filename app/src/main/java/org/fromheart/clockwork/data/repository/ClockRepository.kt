package org.fromheart.clockwork.data.repository

import org.fromheart.clockwork.data.dao.TimeZoneDao
import org.fromheart.clockwork.data.model.TimeZoneModel

class ClockRepository(private val dao: TimeZoneDao) {

    val clockFlow = dao.getClockFlow()

    suspend fun getTimeZoneList(): List<TimeZoneModel> {
        return dao.getTimeZoneList().sortedBy { it.zone }
    }

    suspend fun getTimeZoneList(startWith: String): List<TimeZoneModel> {
        return dao.getTimeZoneList().filter {
            it.zone.length >= startWith.length && it.zone.substring(0, startWith.length).equals(startWith, true)
        }.sortedBy { it.zone }
    }

    suspend fun addTimeZoneList(list: List<String>) {
        if (dao.isEmpty()) {
            dao.insert(
                list.map {
                    val (id, zone) = it.split("=")
                    TimeZoneModel(id = id, zone = zone)
                }
            )
        }
    }

    suspend fun addClock(timeZone: TimeZoneModel) {
        dao.update(timeZone.copy(added = true))
    }

    suspend fun deleteClock(timeZone: TimeZoneModel) {
        dao.update(timeZone.copy(added = false))
    }
}