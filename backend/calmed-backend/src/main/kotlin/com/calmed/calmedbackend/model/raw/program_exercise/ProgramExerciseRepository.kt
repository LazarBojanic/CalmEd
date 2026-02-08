package com.calmed.calmedbackend.model.raw.program_exercise

import com.calmed.calmedbackend.repository.program_exercise.ProgramExerciseRow
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.eq

class ProgramExerciseRepository {

    fun getUpNext(): ProgramExerciseRow? {
        return transaction {
            ProgramExerciseTable
                .selectAll()
                .orderBy(
                    ProgramExerciseTable.weekNumber to SortOrder.ASC,
                    ProgramExerciseTable.orderInWeek to SortOrder.ASC
                )
                .limit(1)
                .map {
                    ProgramExerciseRow(
                        id = it[ProgramExerciseTable.id].value,
                        title = it[ProgramExerciseTable.title],
                        videoUrl = it[ProgramExerciseTable.videoUrl],
                        thumbnailUrl = it[ProgramExerciseTable.thumbnailUrl]
                    )
                }
                .firstOrNull()
        }
    }

    fun getUpNextList(limit: Int = 4): List<ProgramExerciseRow> {
        return transaction {
            ProgramExerciseTable
                .selectAll()
                .orderBy(
                    ProgramExerciseTable.weekNumber to SortOrder.ASC,
                    ProgramExerciseTable.orderInWeek to SortOrder.ASC
                )
                .limit(limit)
                .map {
                    ProgramExerciseRow(
                        id = it[ProgramExerciseTable.id].value,
                        title = it[ProgramExerciseTable.title],
                        videoUrl = it[ProgramExerciseTable.videoUrl],
                        thumbnailUrl = it[ProgramExerciseTable.thumbnailUrl]
                    )
                }
        }
    }

    fun getByWeek(week: Int): List<ProgramExerciseRow> {
        return transaction {
            ProgramExerciseTable
                .selectAll()
                .where { ProgramExerciseTable.weekNumber eq week }
                .orderBy(ProgramExerciseTable.orderInWeek to SortOrder.ASC)
                .map {
                    ProgramExerciseRow(
                        id = it[ProgramExerciseTable.id].value,
                        title = it[ProgramExerciseTable.title],
                        videoUrl = it[ProgramExerciseTable.videoUrl],
                        thumbnailUrl = it[ProgramExerciseTable.thumbnailUrl]
                    )
                }
        }
    }



}
