package com.example.domain.repository

import com.example.data.entity.SpeechChallengeHistoryEntity
import kotlinx.coroutines.flow.Flow

interface SpeechChallengeHistoryRepository {
    fun getAllSpeechChallenges(): Flow<List<SpeechChallengeHistoryEntity>>
    suspend fun insertSpeechChallenge(challenge: SpeechChallengeHistoryEntity)
    suspend fun clearAllSpeechChallenges()
}
