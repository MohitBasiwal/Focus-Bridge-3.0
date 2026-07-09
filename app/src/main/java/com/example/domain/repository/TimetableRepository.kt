package com.example.domain.repository

import com.example.data.entity.TimetableSubjectEntity
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {
    fun getAllSubjects(): Flow<List<TimetableSubjectEntity>>
    suspend fun insertSubject(subject: TimetableSubjectEntity)
    suspend fun updateSubject(subject: TimetableSubjectEntity)
    suspend fun deleteSubject(subject: TimetableSubjectEntity)
}
