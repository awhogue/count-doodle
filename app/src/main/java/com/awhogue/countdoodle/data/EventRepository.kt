package com.awhogue.countdoodle.data

import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val dao: EventDao,
    private val onChanged: () -> Unit = {},
) {
    fun observeAll(): Flow<List<Event>> = dao.observeAll()

    fun observeUpcoming(now: Long, limit: Int): Flow<List<Event>> =
        dao.observeUpcoming(now, limit)

    fun observeById(id: Long): Flow<Event?> = dao.observeById(id)

    suspend fun getUpcoming(now: Long, limit: Int): List<Event> = dao.getUpcoming(now, limit)
    suspend fun getById(id: Long): Event? = dao.getById(id)

    suspend fun upsert(event: Event): Long {
        val id = if (event.id == 0L) dao.insert(event) else { dao.update(event); event.id }
        onChanged()
        return id
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
        onChanged()
    }
}
