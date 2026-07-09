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
    @Singleton
    fun provideFocusRepository(dao: FocusSessionDao): FocusRepository {
        return FocusRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(dao: TimetableSubjectDao): TimetableRepository {
        return TimetableRepositoryImpl(dao)
    }
}
