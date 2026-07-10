package com.example.domain.repository

import com.example.data.entity.EmergencyUnlockEntity
import kotlinx.coroutines.flow.Flow

interface EmergencyUnlockRepository {
    fun getAllUnlocks(): Flow<List<EmergencyUnlockEntity>>
    fun getUnlocksSince(startTime: Long): Flow<List<EmergencyUnlockEntity>>
    suspend fun getUnlocksSinceSync(startTime: Long): List<EmergencyUnlockEntity>
    suspend fun insertUnlock(unlock: EmergencyUnlockEntity)
}
