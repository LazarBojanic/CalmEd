package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.response.CalendarDayDto
import com.calmed.calmedbackend.model.dto.response.CalendarMonthDto
import com.calmed.calmedbackend.model.dto.response.HomeDto
import com.calmed.calmedbackend.model.dto.response.UpNextExerciseDto
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseRepository
import com.calmed.calmedbackend.service.specification.HomeService as HomeServiceSpec
import com.calmed.calmedbackend.service.specification.IUserService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class HomeService(
    private val programExerciseRepository: ProgramExerciseRepository,
    private val userService: IUserService
) : HomeServiceSpec {

    override suspend fun getHome(userId: String, year: Int, month: Int): HomeDto {
        val today: LocalDate = LocalDate.now(ZoneOffset.UTC)
        val totalWeeks = 25

        val userResult = userService.getById(UUID.fromString(userId))
        val user = (userResult as AppResult.Success).data


        val startDate: LocalDate = user.createdAt
            .atZone(ZoneOffset.UTC)
            .toLocalDate()


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

        println("HOME DEBUG userId=$userId createdAt=${user.createdAt} startDate=$startDate today=$today currentWeek=$currentWeek")

        val upNext = programExerciseRepository
            .getUpNextForWeek(currentWeek, limit = 2)
            .map { e ->
                UpNextExerciseDto(
                    id = e.id.toString(),
                    title = e.title,
                    durationSeconds = null,
                    thumbnailUrl = e.thumbnailURL ?: "",
                    videoUrl = e.videoURL ?: ""
                )
            }

        return HomeDto(
            greetingName = null,
            avatarUrl = null,
            calendar = CalendarMonthDto(
                year = ym.year,
                month = ym.monthValue,
                days = calendarDays
            ),
            currentWeek = currentWeek,
            upNext = upNext
        )
    }

    fun calculateUnlockedWeeks(programStart: LocalDateTime, now: LocalDateTime): Int {
        val daysPassed = ChronoUnit.DAYS.between(programStart, now)
        return (daysPassed / 7).toInt() + 1
    }
}
