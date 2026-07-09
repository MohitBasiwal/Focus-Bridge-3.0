package com.example.data.repository

import com.example.data.dao.FocusSessionDao
import com.example.data.entity.FocusSessionEntity
import com.example.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val dao: FocusSessionDao
) : FocusRepository {
    override fun getAllSessions(): Flow<List<FocusSessionEntity>> = dao.getAllSessions()
    override suspend fun insertSession(session: FocusSessionEntity) = dao.insertSession(session)
}
