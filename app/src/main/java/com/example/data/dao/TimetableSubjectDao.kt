package com.example.data.dao

import androidx.room.*
import com.example.data.entity.TimetableSubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableSubjectDao {
    @Query("SELECT * FROM timetable_subjects ORDER BY startTime ASC")
    fun getAllSubjects(): Flow<List<TimetableSubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: TimetableSubjectEntity)

    @Update
    suspend fun updateSubject(subject: TimetableSubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: TimetableSubjectEntity)
}
