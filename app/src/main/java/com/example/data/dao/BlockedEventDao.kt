package com.example.data.dao

import androidx.room.*
import com.example.data.entity.BlockedEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedEventDao {
    @Query("SELECT * FROM blocked_events ORDER BY timestamp DESC")
    fun getAllBlockedEvents(): Flow<List<BlockedEventEntity>>

    @Query("SELECT * FROM blocked_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getBlockedEventsSince(startTime: Long): Flow<List<BlockedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedEvent(event: BlockedEventEntity)

    @Query("DELETE FROM blocked_events")
    suspend fun clearAllBlockedEvents()
}
