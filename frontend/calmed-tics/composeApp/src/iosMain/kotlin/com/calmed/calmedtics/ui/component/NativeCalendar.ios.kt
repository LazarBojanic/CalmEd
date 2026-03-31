package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.UIKit.UICalendarView
import platform.UIKit.UICalendarSelectionSingleDate
import platform.UIKit.UICalendarSelectionSingleDateDelegateProtocol
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeCalendar(
    year: Int,
    month: Int,
    modifier: Modifier,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val delegate = remember {
        object : NSObject(), UICalendarSelectionSingleDateDelegateProtocol {
            override fun dateSelection(
                selection: UICalendarSelectionSingleDate,
                didSelectDate: NSDateComponents?
            ) {
                val selected = didSelectDate ?: return
                onDateSelected(
                    selected.year.toInt(),
                    selected.month.toInt(),
                    selected.day.toInt()
                )
            }
        }
    }
    val selection = remember { UICalendarSelectionSingleDate(delegate = delegate) }
    val calendar = remember { NSCalendar.currentCalendar }

    UIKitView(
        modifier = modifier,
        factory = {
            val calendarView = UICalendarView().apply {
                visibleDateComponents = NSDateComponents().apply {
                    setYear(year.toLong())
                    setMonth(month.toLong())
                    setDay(1)
                }
                selectionBehavior = selection
            }
            val container = UIView()
            container.addSubview(calendarView)
            calendarView.translatesAutoresizingMaskIntoConstraints = false
            calendarView.leadingAnchor.constraintEqualToAnchor(container.leadingAnchor).active = true
            calendarView.trailingAnchor.constraintEqualToAnchor(container.trailingAnchor).active = true
            calendarView.topAnchor.constraintEqualToAnchor(container.topAnchor).active = true
            calendarView.bottomAnchor.constraintEqualToAnchor(container.bottomAnchor).active = true
            container
        },
        update = { container ->
            val calendarView = container.subviews.firstOrNull() as? UICalendarView ?: return@UIKitView
            calendarView.visibleDateComponents = NSDateComponents().apply {
                setYear(year.toLong())
                setMonth(month.toLong())
                setDay(1)
            }
            calendarView.selectionBehavior = selection
            val today = NSDate()
            val components = calendar.components(
                NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
                fromDate = today
            )
            if (components.year.toInt() == year && components.month.toInt() == month) {
                selection.setSelectedDate(components, animated = false)
            }
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}
