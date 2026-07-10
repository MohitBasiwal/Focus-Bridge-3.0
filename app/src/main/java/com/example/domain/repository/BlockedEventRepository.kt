package com.example.domain.repository

import com.example.data.entity.BlockedEventEntity
import kotlinx.coroutines.flow.Flow

interface BlockedEventRepository {
    fun getAllBlockedEvents(): Flow<List<BlockedEventEntity>>
    fun getBlockedEventsSince(startTime: Long): Flow<List<BlockedEventEntity>>
    suspend fun insertBlockedEvent(event: BlockedEventEntity)
    suspend fun clearAllBlockedEvents()
}
