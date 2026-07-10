package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speech_challenge_history")
data class SpeechChallengeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val paragraphText: String,
    val accuracy: Double,
    val isSuccess: Boolean
)
