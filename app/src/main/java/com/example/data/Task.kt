package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubTask(
    val title: String,
    val isCompleted: Boolean = false
)

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, SubTask::class.java)
    private val adapterContext = moshi.adapter<List<SubTask>>(listType)

    @TypeConverter
    fun fromSubTaskList(value: List<SubTask>?): String {
        return adapterContext.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toSubTaskList(value: String?): List<SubTask> {
        if (value.isNullOrEmpty()) return emptyList()
        return adapterContext.fromJson(value) ?: emptyList()
    }
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: Int, // 0 = Low (Green), 1 = Medium (Yellow), 2 = High (Red)
    val category: String, // "Études", "Perso", "Projets", "Santé", "Finance"
    val deadline: Long, // timestamp
    val durationMinutes: Int, // estimated focus duration in mins
    val isCompleted: Boolean = false,
    val recurrence: String = "none", // "none", "daily", "weekly", "monthly"
    val subtasks: List<SubTask> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
