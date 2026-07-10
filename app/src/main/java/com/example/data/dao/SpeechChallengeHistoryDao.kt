package com.example.data.dao

import androidx.room.*
import com.example.data.entity.SpeechChallengeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechChallengeHistoryDao {
    @Query("SELECT * FROM speech_challenge_history ORDER BY timestamp DESC")
    fun getAllSpeechChallenges(): Flow<List<SpeechChallengeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeechChallenge(challenge: SpeechChallengeHistoryEntity)

    @Query("DELETE FROM speech_challenge_history")
    suspend fun clearAllSpeechChallenges()
}
