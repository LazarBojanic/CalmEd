package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmed.calmedtics.model.dto.response.HomeDto
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import com.calmed.calmedtics.repository.HomeRepository
import com.calmed.calmedtics.repository.ProgressRepository
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.home_load_failed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class HomeViewModel(
	private val homeRepository: HomeRepository,
	private val progressRepository: ProgressRepository,
) : ViewModel() {

	private val _home = MutableStateFlow<HomeDto?>(null)
	val home: StateFlow<HomeDto?> = _home

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	val allCompletions: StateFlow<List<ExerciseCompletionEntity>> =
		progressRepository.completions
			.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	suspend fun load(year: Int, month: Int) {
		_error.value = null
		_loading.value = true
		try {
			val result = homeRepository.getHome(year, month)
			_home.value = result
			if (result != null) {
				progressRepository.syncFromHome(result.completions)
			}
		} catch (t: CancellationException) {
			throw t
		} catch (t: Throwable) {
			_error.value = getString(Res.string.home_load_failed)
		} finally {
			_loading.value = false
		}
	}

	suspend fun markSessionCompleted(
		week: Int,
		day: Int,
		userId: String,
		session: String,
		completed: Boolean
	) {
		progressRepository.setCompleted(week, day, userId, session, completed)
		viewModelScope.launch {
			try {
				progressRepository.syncProgress(week, day, session, completed)
			} catch (e: CancellationException) {
				throw e
			} catch (e: Exception) {
				// Progress sync is best-effort; local state is already persisted.
			}
		}
	}
}
