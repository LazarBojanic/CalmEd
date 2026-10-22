package com.calmed.calmedtics.ui.component.exercises

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.theme.blue006
import com.calmed.calmedtics.theme.blue008
import com.calmed.calmedtics.theme.blue009
import com.calmed.calmedtics.theme.gray006
import com.calmed.calmedtics.theme.green006
import com.calmed.calmedtics.theme.purple006
import com.calmed.calmedtics.theme.red005
import com.calmed.calmedtics.theme.red006
import com.calmed.calmedtics.theme.turquoise006
import com.calmed.calmedtics.theme.yellow006

fun groupAccent(id: Int): Brush {
    val base = when (id) {
        0 -> gray006
        1 -> yellow006
        2 -> turquoise006
        3 -> green006
        4 -> blue008
        5 -> red006
        else -> gray006
    }
    return Brush.verticalGradient(
        colors = listOf(base, lerp(base, Color.White, 0.35f))
    )
}

fun groupIdForWeek(week: Int): Int {
    return when {
        week <= 0 -> 0
        week <= 4 -> 1
        week <= 12 -> 2
        week <= 18 -> 3
        week <= 24 -> 4
        else -> 5
    }
}

fun isGroupLocked(items: List<ProgramExerciseDto>, currentWeek: Int): Boolean {
    val minWeek = items.minOfOrNull { it.weekNumber } ?: return false
    return minWeek > currentWeek
}
