package com.example.data.repository

import com.example.data.dao.BlockedAppDao
import com.example.data.entity.BlockedAppEntity
import com.example.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppRepositoryImpl @Inject constructor(
    private val dao: BlockedAppDao
) : BlockedAppRepository {
    override fun getAllBlockedApps(): Flow<List<BlockedAppEntity>> = dao.getAllBlockedApps()
    override suspend fun getAllBlockedAppsSync(): List<BlockedAppEntity> = dao.getAllBlockedAppsSync()
    override suspend fun insertBlockedApp(app: BlockedAppEntity) = dao.insertBlockedApp(app)
    override suspend fun deleteBlockedApp(app: BlockedAppEntity) = dao.deleteBlockedApp(app)
    override suspend fun isAppBlocked(packageName: String): Boolean = dao.isAppBlocked(packageName)
}
