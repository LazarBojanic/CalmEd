package com.calmed.calmedtics.repository

import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.service.specification.IVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DownloadedVideosRepository(
	private val downloads: IVideoDownloadManager,
	private val exercises: ExercisesRepository,
) {
	val downloadedExercises: Flow<List<ProgramExerciseDto>> =
		combine(downloads.states, exercises.exercises) { states, all ->
			val downloaded = states
				.filterValues { it.status == VideoDownloadStatus.Downloaded }
				.keys
			all.filter { it.playbackId in downloaded }
		}

	suspend fun exerciseFor(playbackId: String): ProgramExerciseDto? =
		exercises.findByPlaybackId(playbackId)

	suspend fun removeOrphans() {
		val known = exercises.allPlaybackIds()
		downloads.states.value.keys
			.filterNot { it in known }
			.forEach { downloads.remove(it) }
	}
}
