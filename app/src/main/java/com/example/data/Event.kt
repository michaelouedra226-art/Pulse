package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startTime: Long, // timestamp
    val endTime: Long, // timestamp
    val colorHex: Int, // custom color for event styling
    val location: String = "" // place
)
