package com.example.data.dao

import androidx.room.*
import com.example.data.entity.BlockedWebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedWebsiteDao {
    @Query("SELECT * FROM blocked_websites ORDER BY domain ASC")
    fun getAllBlockedWebsites(): Flow<List<BlockedWebsiteEntity>>

    @Query("SELECT * FROM blocked_websites ORDER BY domain ASC")
    suspend fun getAllBlockedWebsitesSync(): List<BlockedWebsiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedWebsite(website: BlockedWebsiteEntity)

    @Delete
    suspend fun deleteBlockedWebsite(website: BlockedWebsiteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_websites WHERE domain = :domain LIMIT 1)")
    suspend fun isWebsiteBlocked(domain: String): Boolean
}
