package com.example.data.repository

import com.example.data.dao.SpeechChallengeHistoryDao
import com.example.data.entity.SpeechChallengeHistoryEntity
import com.example.domain.repository.SpeechChallengeHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SpeechChallengeHistoryRepositoryImpl @Inject constructor(
    private val dao: SpeechChallengeHistoryDao
) : SpeechChallengeHistoryRepository {
    override fun getAllSpeechChallenges(): Flow<List<SpeechChallengeHistoryEntity>> = dao.getAllSpeechChallenges()

    override suspend fun insertSpeechChallenge(challenge: SpeechChallengeHistoryEntity) = dao.insertSpeechChallenge(challenge)

    override suspend fun clearAllSpeechChallenges() = dao.clearAllSpeechChallenges()
}
