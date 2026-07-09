package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.TimetableSubjectEntity
import com.example.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    // Expose all subjects reactively, ordered by start time
    val timetableSubjects: StateFlow<List<TimetableSubjectEntity>> = repository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSubject(
        name: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        colorArgb: Int,
        category: String,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = TimetableSubjectEntity(
                name = name,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                colorArgb = colorArgb,
                category = category,
                notes = notes
            )
            repository.insertSubject(entity)
        }
    }

    fun updateSubject(subject: TimetableSubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: TimetableSubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }
}
