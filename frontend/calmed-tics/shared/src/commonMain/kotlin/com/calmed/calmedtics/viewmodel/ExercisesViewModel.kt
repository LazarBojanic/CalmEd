package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmed.calmedtics.model.dto.response.ExerciseGroupDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.repository.ExercisesRepository
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.exercises_load_failed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.getString
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
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = getString(Res.string.exercises_load_failed)
		} finally {
			_loading.value = false
		}
	}
}
