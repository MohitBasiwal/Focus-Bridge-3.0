package com.example.domain.repository

import com.example.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun getAllSessions(): Flow<List<FocusSessionEntity>>
    suspend fun insertSession(session: FocusSessionEntity)
}
