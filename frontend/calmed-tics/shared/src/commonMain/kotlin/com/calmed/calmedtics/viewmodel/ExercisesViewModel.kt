package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmed.calmedtics.model.dto.response.ExerciseGroupDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.repository.ExercisesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExercisesViewModel(
	private val repository: ExercisesRepository,
) : ViewModel() {

	val exercises: StateFlow<List<ProgramExerciseDto>> =
		repository.exercises
			.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	val groups: StateFlow<List<ExerciseGroupDto>> =
		repository.groups
			.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	suspend fun load() {
		_loading.value = true
		_error.value = null
		try {
			repository.refresh()
		} catch (t: Throwable) {
			_error.value = t.message ?: "Failed to load exercises."
		} finally {
			_loading.value = false
		}
	}
}
