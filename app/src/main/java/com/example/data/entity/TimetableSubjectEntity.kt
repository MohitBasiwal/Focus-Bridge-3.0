package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_subjects")
data class TimetableSubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val startTime: String, // "HH:mm"
    val endTime: String,   // "HH:mm"
    val colorArgb: Int,    // Color integer
    val category: String = "Study",
    val notes: String = "",
    val blockedApps: String = "",
    val blockedWebsites: String = ""
)
