package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.entity.FocusSessionEntity
import com.example.data.dao.FocusSessionDao
import com.example.data.entity.TimetableSubjectEntity
import com.example.data.dao.TimetableSubjectDao

@Database(entities = [FocusSessionEntity::class, TimetableSubjectEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun timetableSubjectDao(): TimetableSubjectDao
}
