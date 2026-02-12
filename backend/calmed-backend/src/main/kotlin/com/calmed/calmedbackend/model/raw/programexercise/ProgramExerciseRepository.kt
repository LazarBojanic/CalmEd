package com.calmed.calmedbackend.model.raw.programexercise

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ProgramExerciseRepository {

    suspend fun getUpNextForWeek(
        weekNumber: Int,
        limit: Int
    ): List<ProgramExercise> =
        transaction {
            ProgramExerciseEntity
                .find { ProgramExerciseTable.weekNumber eq weekNumber }
                .orderBy(ProgramExerciseTable.orderInWeek to SortOrder.ASC)
                .limit(limit)
                .map { e ->
                    ProgramExercise(
                        id = e.id.value,
                        weekNumber = e.weekNumber,
                        title = e.title,
                        description = e.description,
                        videoURL = e.videoURL,
                        thumbnailURL = e.thumbnailURL,
                        orderInWeek = e.orderInWeek,
                        createdAt = e.createdAt,
                        updatedAt = e.updatedAt
                    )
                }
        }
}
