package com.example.data.repository

import com.example.data.dao.EmergencyUnlockDao
import com.example.data.entity.EmergencyUnlockEntity
import com.example.domain.repository.EmergencyUnlockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyUnlockRepositoryImpl @Inject constructor(
    private val dao: EmergencyUnlockDao
) : EmergencyUnlockRepository {
    override fun getAllUnlocks(): Flow<List<EmergencyUnlockEntity>> = dao.getAllUnlocks()
    override fun getUnlocksSince(startTime: Long): Flow<List<EmergencyUnlockEntity>> = dao.getUnlocksSince(startTime)
    override suspend fun getUnlocksSinceSync(startTime: Long): List<EmergencyUnlockEntity> = dao.getUnlocksSinceSync(startTime)
    override suspend fun insertUnlock(unlock: EmergencyUnlockEntity) = dao.insertUnlock(unlock)
}
