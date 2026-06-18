package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseDao {
    // Tasks queries
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // Focus Sessions queries
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long

    @Delete
    suspend fun deleteFocusSession(session: FocusSession)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllFocusSessions()

    // Events queries
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
