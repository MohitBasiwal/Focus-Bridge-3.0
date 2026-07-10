package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_events")
data class BlockedEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val identifier: String, // package name or website domain
    val displayName: String,
    val timestamp: Long,
    val isApp: Boolean
)
