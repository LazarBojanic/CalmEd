package com.calmed.calmedfrontendtourettes.ui.component

import android.widget.CalendarView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Calendar
import java.util.GregorianCalendar

@Composable
actual fun NativeCalendar(
    year: Int,
    month: Int,
    modifier: Modifier,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            CalendarView(context).apply {
                val cal = GregorianCalendar(year, month - 1, 1)
                date = resolveDisplayDateMillis(year, month, cal)
                setOnDateChangeListener { _, y, m, d ->
                    onDateSelected(y, m + 1, d)
                }
            }
        },
        update = { view ->
            val cal = GregorianCalendar(year, month - 1, 1)
            view.date = resolveDisplayDateMillis(year, month, cal)
        }
    )
}

private fun resolveDisplayDateMillis(
    year: Int,
    month: Int,
    fallback: Calendar
): Long {
    val now = Calendar.getInstance()
    return if (now.get(Calendar.YEAR) == year && (now.get(Calendar.MONTH) + 1) == month) {
        now.timeInMillis
    } else {
        fallback.timeInMillis
    }
}
