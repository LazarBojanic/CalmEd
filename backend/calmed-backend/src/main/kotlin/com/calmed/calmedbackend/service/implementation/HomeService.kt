package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.ProgramConstants
import com.calmed.calmedbackend.model.dto.response.CalendarDayDto
import com.calmed.calmedbackend.model.dto.response.CalendarMonthDto
import com.calmed.calmedbackend.model.dto.response.HomeDto
import com.calmed.calmedbackend.service.specification.IHomeService
import com.calmed.calmedbackend.service.specification.IUserProgramService
import com.calmed.calmedbackend.service.specification.IUserService
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import io.ktor.http.HttpStatusCode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class HomeService(
    private val userService: IUserService,
    private val userProgramService: IUserProgramService,
    private val userExerciseProgressRepository: IUserExerciseProgressRepository
) : IHomeService {

    override suspend fun getHome(userId: String, year: Int, month: Int): HomeDto {
        return withTransaction {
        val uid = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            throw BusinessException(HttpStatusCode.BadRequest, "Invalid user id.")
        }
        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
        val totalWeeks = ProgramConstants.TOTAL_WEEKS

        val user = when (val userResult = userService.getById(uid)) {
            is AppResult.Success -> userResult.data
            is AppResult.Failure -> throw BusinessException(HttpStatusCode.NotFound, "User not found.")
        }

        val userProgram = (userProgramService.getByUserId(user.id) as? AppResult.Success)?.data
        val startDate: LocalDate = userProgram?.startDate ?: user.createdAt
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        val progressList = userExerciseProgressRepository.findAllByUserId(uid)
        val completions = progressList.filter { it.completedAt != null }.map {
            com.calmed.calmedbackend.model.dto.response.UserExerciseProgressCompactDto(
                week = it.week,
                day = it.day,
                session = it.exerciseSession,
                completed = true
            )
        }


        val daysFromStartToday: Long =
            ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0)

        var currentWeek: Int = (daysFromStartToday / 7).toInt() + 1
        if (currentWeek > totalWeeks) currentWeek = totalWeeks

        val ym = YearMonth.of(year, month)
        val daysInMonth = ym.lengthOfMonth()

        val calendarDays = (1..daysInMonth).map { day ->
            val date: LocalDate = ym.atDay(day)

            val status = when {
                date.isBefore(startDate) -> "BEFORE_START"
                else -> {
                    val daysFromStart: Long = ChronoUnit.DAYS.between(startDate, date).coerceAtLeast(0)
                    val weekOfDate: Int = (daysFromStart / 7).toInt() + 1

                    when {
                        weekOfDate < currentWeek -> "DONE"
                        weekOfDate == currentWeek -> "AVAILABLE"
                        else -> "LOCKED"
                    }
                }
            }

            CalendarDayDto(
                day = day,
                status = status
            )
        }

        HomeDto(
            greetingName = null,
            avatarUrl = null,
            calendar = CalendarMonthDto(
                year = ym.year,
                month = ym.monthValue,
                days = calendarDays
            ),
            currentWeek = currentWeek,
            programStartDate = startDate.toString(),
            completions = completions
        )
        }
    }

    fun calculateUnlockedWeeks(programStart: LocalDateTime, now: LocalDateTime): Int {
        val daysPassed = ChronoUnit.DAYS.between(programStart, now)
        return (daysPassed / 7).toInt() + 1
    }
}
