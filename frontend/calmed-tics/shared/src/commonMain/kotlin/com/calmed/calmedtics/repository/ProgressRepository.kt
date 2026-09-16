package com.calmed.calmedtics.repository

import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.UserExerciseProgressUpdateDto
import com.calmed.calmedtics.model.dto.response.UserExerciseProgressCompactDto
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import com.calmed.calmedtics.model.raw.ExerciseSession
import com.calmed.calmedtics.store.ITokenDataStore
import com.calmed.calmedtics.util.currentTimeMillis
import com.calmed.calmedtics.util.currentUserId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class ProgressRepository(
	private val api: IAppApi,
	private val exerciseCompletionDao: IExerciseCompletionDao,
	private val tokenStore: ITokenDataStore,
) {
	val completions: Flow<List<ExerciseCompletionEntity>> =
		exerciseCompletionDao.getAllCompletions()

	suspend fun syncFromHome(completions: List<UserExerciseProgressCompactDto>) {
		val userId = tokenStore.currentUserId() ?: return
		completions.forEach { comp ->
			val session = comp.session.name
			val existing =
				exerciseCompletionDao.findExisting(comp.week, userId, comp.day, session)
			if (existing == null) {
				exerciseCompletionDao.upsert(
					ExerciseCompletionEntity(
						week = comp.week,
						userId = userId,
						day = comp.day,
						session = session,
						completed = true,
						timestamp = currentTimeMillis()
					)
				)
			}
		}
	}

	suspend fun setCompleted(
		week: Int,
		day: Int,
		userId: String,
		session: String,
		completed: Boolean,
	) {
		val sessionKey = parseSession(session).name
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
	}

	suspend fun syncProgress(
		week: Int,
		day: Int,
		session: String,
		completed: Boolean,
	) {
		api.syncExerciseProgress(
			UserExerciseProgressUpdateDto(
				week = week,
				day = day,
				session = parseSession(session),
				completed = completed
			)
		)
	}

	private fun parseSession(session: String): ExerciseSession = try {
		ExerciseSession.valueOf(session.uppercase())
	} catch (e: Exception) {
		ExerciseSession.MORNING
	}
}
