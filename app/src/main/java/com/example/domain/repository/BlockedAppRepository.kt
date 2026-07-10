package com.example.domain.repository

import com.example.data.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

interface BlockedAppRepository {
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>
    suspend fun getAllBlockedAppsSync(): List<BlockedAppEntity>
    suspend fun insertBlockedApp(app: BlockedAppEntity)
    suspend fun deleteBlockedApp(app: BlockedAppEntity)
    suspend fun isAppBlocked(packageName: String): Boolean
}
