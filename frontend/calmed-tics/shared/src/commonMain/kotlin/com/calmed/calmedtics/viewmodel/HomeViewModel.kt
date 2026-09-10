package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.UserExerciseProgressUpdateDto
import com.calmed.calmedtics.model.dto.response.HomeDto
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import com.calmed.calmedtics.model.raw.ExerciseSession
import com.calmed.calmedtics.repository.HomeRepository
import com.calmed.calmedtics.repository.IExerciseCompletionDao
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.util.currentTimeMillis
import com.calmed.calmedtics.util.currentUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
	private val api: IAppApi,
	private val homeRepository: HomeRepository,
	private val exerciseCompletionDao: IExerciseCompletionDao,
	private val tokenStore: ITokenDataStore,
) : ViewModel() {

	private val _home = MutableStateFlow<HomeDto?>(null)
	val home: StateFlow<HomeDto?> = _home

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	val allCompletions: StateFlow<List<ExerciseCompletionEntity>> =
		exerciseCompletionDao.getAllCompletions()
			.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	suspend fun load(year: Int, month: Int) {
		_error.value = null
		_loading.value = true
		try {
			val result = homeRepository.getHome(year, month)
			_home.value = result
			if (result != null) {
				syncCompletions(result)
			}
		} catch (t: Throwable) {
			_error.value = t.message ?: "Failed to load home."
		} finally {
			_loading.value = false
		}
	}

	private suspend fun syncCompletions(result: HomeDto) {
		val userId = tokenStore.currentUserId() ?: return
		result.completions.forEach { comp ->
			val sessionStr = comp.session.name.uppercase()
			val existing =
				exerciseCompletionDao.findExisting(comp.week, userId, comp.day, sessionStr)
			if (existing == null) {
				exerciseCompletionDao.upsert(
					ExerciseCompletionEntity(
						week = comp.week,
						userId = userId,
						day = comp.day,
						session = sessionStr,
						completed = true,
						timestamp = currentTimeMillis()
					)
				)
			}
		}
	}

	suspend fun markSessionCompleted(
		week: Int,
		day: Int,
		userId: String,
		session: String,
		completed: Boolean
	) {
		val exerciseSession = try {
			ExerciseSession.valueOf(session.uppercase())
		} catch (e: Exception) {
			ExerciseSession.MORNING
		}

		val sessionKey = exerciseSession.name

		if (completed) {
			exerciseCompletionDao.upsert(
				ExerciseCompletionEntity(
					week = week,
					day = day,
					userId = userId,
					session = sessionKey,
					completed = true,
					timestamp = currentTimeMillis()
				)
			)
		} else {
			exerciseCompletionDao.delete(week, userId, day, sessionKey)
		}

		viewModelScope.launch {
			try {
				api.syncExerciseProgress(
					UserExerciseProgressUpdateDto(
						week = week,
						day = day,
						session = exerciseSession,
						completed = completed
					)
				)
			} catch (e: Exception) {
				// Progress sync is best-effort; local state is already persisted.
			}
		}
	}
}
