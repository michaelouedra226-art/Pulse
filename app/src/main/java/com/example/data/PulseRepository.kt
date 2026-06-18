package com.example.data

import kotlinx.coroutines.flow.Flow

class PulseRepository(private val pulseDao: PulseDao) {
    val allTasks: Flow<List<Task>> = pulseDao.getAllTasks()
    val allFocusSessions: Flow<List<FocusSession>> = pulseDao.getAllFocusSessions()
    val allEvents: Flow<List<Event>> = pulseDao.getAllEvents()

    suspend fun insertTask(task: Task) {
        pulseDao.insertTask(task)
    }

    suspend fun deleteTask(task: Task) {
        pulseDao.deleteTask(task)
    }

    suspend fun deleteAllTasks() {
        pulseDao.deleteAllTasks()
    }

    suspend fun insertFocusSession(session: FocusSession) {
        pulseDao.insertFocusSession(session)
    }

    suspend fun deleteFocusSession(session: FocusSession) {
        pulseDao.deleteFocusSession(session)
    }

    suspend fun deleteAllFocusSessions() {
        pulseDao.deleteAllFocusSessions()
    }

    suspend fun insertEvent(event: Event) {
        pulseDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        pulseDao.deleteEvent(event)
    }

    suspend fun deleteAllEvents() {
        pulseDao.deleteAllEvents()
    }
}
