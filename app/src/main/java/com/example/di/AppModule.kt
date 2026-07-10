package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.dao.FocusSessionDao
import com.example.data.dao.TimetableSubjectDao
import com.example.data.repository.FocusRepositoryImpl
import com.example.data.repository.TimetableRepositoryImpl
import com.example.domain.repository.FocusRepository
import com.example.domain.repository.TimetableRepository
import com.example.data.dao.EmergencyUnlockDao
import com.example.domain.repository.EmergencyUnlockRepository
import com.example.data.repository.EmergencyUnlockRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "focus_bridge.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao {
        return database.focusSessionDao()
    }

    @Provides
    fun provideTimetableSubjectDao(database: AppDatabase): TimetableSubjectDao {
        return database.timetableSubjectDao()
    }

    @Provides
    fun provideBlockedAppDao(database: AppDatabase): com.example.data.dao.BlockedAppDao {
        return database.blockedAppDao()
    }

    @Provides
    @Singleton
    fun provideFocusRepository(dao: FocusSessionDao): FocusRepository {
        return FocusRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideBlockedAppRepository(dao: com.example.data.dao.BlockedAppDao): com.example.domain.repository.BlockedAppRepository {
        return com.example.data.repository.BlockedAppRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(dao: TimetableSubjectDao): TimetableRepository {
        return TimetableRepositoryImpl(dao)
    }

    @Provides
    fun provideEmergencyUnlockDao(database: AppDatabase): EmergencyUnlockDao {
        return database.emergencyUnlockDao()
    }

    @Provides
    @Singleton
    fun provideEmergencyUnlockRepository(dao: EmergencyUnlockDao): EmergencyUnlockRepository {
        return EmergencyUnlockRepositoryImpl(dao)
    }

    @Provides
    fun provideBlockedWebsiteDao(database: AppDatabase): com.example.data.dao.BlockedWebsiteDao {
        return database.blockedWebsiteDao()
    }

    @Provides
    @Singleton
    fun provideBlockedWebsiteRepository(dao: com.example.data.dao.BlockedWebsiteDao): com.example.domain.repository.BlockedWebsiteRepository {
        return com.example.data.repository.BlockedWebsiteRepositoryImpl(dao)
    }

    @Provides
    fun provideBlockedEventDao(database: AppDatabase): com.example.data.dao.BlockedEventDao {
        return database.blockedEventDao()
    }

    @Provides
    @Singleton
    fun provideBlockedEventRepository(dao: com.example.data.dao.BlockedEventDao): com.example.domain.repository.BlockedEventRepository {
        return com.example.data.repository.BlockedEventRepositoryImpl(dao)
    }

    @Provides
    fun provideSpeechChallengeHistoryDao(database: AppDatabase): com.example.data.dao.SpeechChallengeHistoryDao {
        return database.speechChallengeHistoryDao()
    }

    @Provides
    @Singleton
    fun provideSpeechChallengeHistoryRepository(dao: com.example.data.dao.SpeechChallengeHistoryDao): com.example.domain.repository.SpeechChallengeHistoryRepository {
        return com.example.data.repository.SpeechChallengeHistoryRepositoryImpl(dao)
    }
}
