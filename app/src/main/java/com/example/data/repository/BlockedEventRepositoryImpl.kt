package com.example.data.repository

import com.example.data.dao.BlockedEventDao
import com.example.data.entity.BlockedEventEntity
import com.example.domain.repository.BlockedEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlockedEventRepositoryImpl @Inject constructor(
    private val dao: BlockedEventDao
) : BlockedEventRepository {
    override fun getAllBlockedEvents(): Flow<List<BlockedEventEntity>> = dao.getAllBlockedEvents()

    override fun getBlockedEventsSince(startTime: Long): Flow<List<BlockedEventEntity>> = dao.getBlockedEventsSince(startTime)

    override suspend fun insertBlockedEvent(event: BlockedEventEntity) = dao.insertBlockedEvent(event)

    override suspend fun clearAllBlockedEvents() = dao.clearAllBlockedEvents()
}
