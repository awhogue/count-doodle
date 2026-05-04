package com.awhogue.countdoodle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY dateEpochMillis ASC")
    fun observeAll(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE dateEpochMillis >= :nowEpochMillis ORDER BY dateEpochMillis ASC LIMIT :limit")
    fun observeUpcoming(nowEpochMillis: Long, limit: Int): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE dateEpochMillis >= :nowEpochMillis ORDER BY dateEpochMillis ASC LIMIT :limit")
    suspend fun getUpcoming(nowEpochMillis: Long, limit: Int): List<Event>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): Event?

    @Query("SELECT * FROM events WHERE id = :id")
    fun observeById(id: Long): Flow<Event?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
