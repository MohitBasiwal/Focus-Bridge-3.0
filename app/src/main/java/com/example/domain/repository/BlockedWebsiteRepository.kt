package com.example.domain.repository

import com.example.data.entity.BlockedWebsiteEntity
import kotlinx.coroutines.flow.Flow

interface BlockedWebsiteRepository {
    fun getAllBlockedWebsites(): Flow<List<BlockedWebsiteEntity>>
    suspend fun getAllBlockedWebsitesSync(): List<BlockedWebsiteEntity>
    suspend fun insertBlockedWebsite(website: BlockedWebsiteEntity)
    suspend fun deleteBlockedWebsite(website: BlockedWebsiteEntity)
    suspend fun isWebsiteBlocked(domain: String): Boolean
}
