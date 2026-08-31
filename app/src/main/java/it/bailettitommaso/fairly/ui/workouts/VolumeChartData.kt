package it.bailettitommaso.fairly.ui.workouts

import it.bailettitommaso.fairly.domain.model.WorkoutSession
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/** Total training volume for the week beginning [weekStart]. */
data class WeeklyVolume(val weekStart: LocalDate, val volume: Double)

/**
 * Buckets sessions into the last [weeks] calendar weeks, most recent last. Weeks with no training
 * are kept as empty buckets: a gap in the chart is itself information.
 *
 * Kept out of the composable so the arithmetic can be tested without rendering anything.
 */
fun weeklyVolume(
    sessions: List<WorkoutSession>,
    today: LocalDate,
    weeks: Int = DEFAULT_WEEKS,
    zone: ZoneId = ZoneId.systemDefault(),
): List<WeeklyVolume> {
    val currentWeekStart = today.mondayOfWeek()
    val firstWeekStart = currentWeekStart.minusWeeks((weeks - 1).toLong())

    val volumeByWeek = sessions
        .groupBy { it.startedAt.atZone(zone).toLocalDate().mondayOfWeek() }
        .mapValues { (_, weekSessions) -> weekSessions.sumOf { it.totalVolume } }

    return (0 until weeks).map { offset ->
        val weekStart = firstWeekStart.plusWeeks(offset.toLong())
        WeeklyVolume(weekStart = weekStart, volume = volumeByWeek[weekStart] ?: 0.0)
    }
}

private fun LocalDate.mondayOfWeek(): LocalDate =
    minusDays((dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

const val DEFAULT_WEEKS = 8
