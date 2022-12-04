package org.fromheart.clockwork.data.repository

import org.fromheart.clockwork.data.dao.TimeZoneDao
import org.fromheart.clockwork.data.model.TimeZoneModel

class ClockRepository(private val dao: TimeZoneDao) {

    val timeZoneFlow = dao.getTimeZoneFlow()

    val clockFlow = dao.getClockFlow()

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