package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.entity.EmergencyUnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyUnlockDao {
    @Query("SELECT * FROM emergency_unlocks ORDER BY timestamp DESC")
    fun getAllUnlocks(): Flow<List<EmergencyUnlockEntity>>

    @Query("SELECT * FROM emergency_unlocks WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getUnlocksSince(startTime: Long): Flow<List<EmergencyUnlockEntity>>

    @Query("SELECT * FROM emergency_unlocks WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getUnlocksSinceSync(startTime: Long): List<EmergencyUnlockEntity>

    @Insert
    suspend fun insertUnlock(unlock: EmergencyUnlockEntity)
}
