package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun NativeCalendar(
    year: Int,
    month: Int,
    modifier: Modifier,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val initialDate = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)

        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        if (currentYear == year && currentMonth == month) {
            set(Calendar.DAY_OF_MONTH, currentDay)
        } else {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.timeInMillis,
        initialDisplayedMonthMillis = initialDate.timeInMillis
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val cal = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = millis
            }
            onDateSelected(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    val currentTypography = MaterialTheme.typography
    val smallCalendarTypography = currentTypography.copy(
        bodyLarge = currentTypography.bodyLarge.copy(fontSize = 14.sp),
        labelMedium = currentTypography.labelMedium.copy(fontSize = 14.sp),
        labelLarge = currentTypography.labelLarge.copy(fontSize = 14.sp),
        titleMedium = currentTypography.titleMedium.copy(fontSize = 16.sp),
        headlineLarge = currentTypography.headlineLarge.copy(fontSize = 18.sp),
        headlineMedium = currentTypography.headlineMedium.copy(fontSize = 16.sp)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        MaterialTheme(typography = smallCalendarTypography) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .scale(0.90f)
                    .padding(horizontal = 0.dp),
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors()
            )
        }
    }
}
