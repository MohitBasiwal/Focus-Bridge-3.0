package com.example.data.repository

import com.example.data.dao.TimetableSubjectDao
import com.example.data.entity.TimetableSubjectEntity
import com.example.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepositoryImpl @Inject constructor(
    private val dao: TimetableSubjectDao
) : TimetableRepository {
    override fun getAllSubjects(): Flow<List<TimetableSubjectEntity>> = dao.getAllSubjects()
    override suspend fun getAllSubjectsSync(): List<TimetableSubjectEntity> = dao.getAllSubjectsSync()
    override suspend fun insertSubject(subject: TimetableSubjectEntity) = dao.insertSubject(subject)
    override suspend fun updateSubject(subject: TimetableSubjectEntity) = dao.updateSubject(subject)
    override suspend fun deleteSubject(subject: TimetableSubjectEntity) = dao.deleteSubject(subject)
}
