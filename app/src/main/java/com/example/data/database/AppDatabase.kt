package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.entity.FocusSessionEntity
import com.example.data.dao.FocusSessionDao
import com.example.data.entity.TimetableSubjectEntity
import com.example.data.dao.TimetableSubjectDao
import com.example.data.entity.BlockedAppEntity
import com.example.data.dao.BlockedAppDao
import com.example.data.entity.EmergencyUnlockEntity
import com.example.data.dao.EmergencyUnlockDao
import com.example.data.entity.BlockedWebsiteEntity
import com.example.data.dao.BlockedWebsiteDao
import com.example.data.entity.BlockedEventEntity
import com.example.data.dao.BlockedEventDao
import com.example.data.entity.SpeechChallengeHistoryEntity
import com.example.data.dao.SpeechChallengeHistoryDao

@Database(
    entities = [
        FocusSessionEntity::class, 
        TimetableSubjectEntity::class, 
        BlockedAppEntity::class,
        EmergencyUnlockEntity::class,
        BlockedWebsiteEntity::class,
        BlockedEventEntity::class,
        SpeechChallengeHistoryEntity::class
    ],
    version = 4, // Increment version for schema change
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun timetableSubjectDao(): TimetableSubjectDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun emergencyUnlockDao(): EmergencyUnlockDao
    abstract fun blockedWebsiteDao(): BlockedWebsiteDao
    abstract fun blockedEventDao(): BlockedEventDao
    abstract fun speechChallengeHistoryDao(): SpeechChallengeHistoryDao
}

