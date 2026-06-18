package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int?, // Linked task ID, null if generic session
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String // "focus", "short_break", "long_break"
)
