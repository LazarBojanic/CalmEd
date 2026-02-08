package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.response.CalendarDayDto
import com.calmed.calmedbackend.model.dto.response.CalendarMonthDto
import com.calmed.calmedbackend.model.dto.response.HomeDto
import com.calmed.calmedbackend.model.dto.response.UpNextExerciseDto
import com.calmed.calmedbackend.model.raw.program_exercise.ProgramExerciseRepository
import com.calmed.calmedbackend.service.specification.HomeService
import com.calmed.calmedbackend.service.specification.IUserService
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId


class HomeService(
    private val programExerciseRepository: ProgramExerciseRepository,
    private val userService: IUserService
) : HomeService {

    override suspend fun getHome(userId: String): HomeDto {
        val today = LocalDate.now()


        val userResult = userService.getById(UUID.fromString(userId))
        val user = (userResult as AppResult.Success).data

        val startDate = user.createdAt
            .atZone(ZoneId.systemDefault())
            .toLocalDate()


        val daysSinceStart = ChronoUnit.DAYS.between(startDate, today).toInt()


        val ym = YearMonth.from(today)
        val daysInMonth = ym.lengthOfMonth()

        val calendarDays = (1..daysInMonth).map { day ->
            val date = ym.atDay(day)
            val dayIndex = ChronoUnit.DAYS.between(startDate, date).toInt()

            val status =
                when {
                    date.isAfter(today) -> "LOCKED"
                    date.isBefore(startDate) -> "LOCKED"
                    else -> "AVAILABLE"
                }

            CalendarDayDto(
                day = day,
                status = status
            )
        }


        val upNext = programExerciseRepository.getUpNextList(limit = 2).map { row ->
            UpNextExerciseDto(
                id = row.id.toString(),
                title = row.title,
                durationSeconds = null,
                thumbnailUrl = row.thumbnailUrl,
                videoUrl = row.videoUrl
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
            upNext = upNext
        )
    }

}
