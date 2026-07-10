package com.example.data.repository

import com.example.data.dao.BlockedWebsiteDao
import com.example.data.entity.BlockedWebsiteEntity
import com.example.domain.repository.BlockedWebsiteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlockedWebsiteRepositoryImpl @Inject constructor(
    private val dao: BlockedWebsiteDao
) : BlockedWebsiteRepository {
    override fun getAllBlockedWebsites(): Flow<List<BlockedWebsiteEntity>> = dao.getAllBlockedWebsites()

    override suspend fun getAllBlockedWebsitesSync(): List<BlockedWebsiteEntity> = dao.getAllBlockedWebsitesSync()

    override suspend fun insertBlockedWebsite(website: BlockedWebsiteEntity) = dao.insertBlockedWebsite(website)

    override suspend fun deleteBlockedWebsite(website: BlockedWebsiteEntity) = dao.deleteBlockedWebsite(website)

    override suspend fun isWebsiteBlocked(domain: String): Boolean = dao.isWebsiteBlocked(domain)
}
