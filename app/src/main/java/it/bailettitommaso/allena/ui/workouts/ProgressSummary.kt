package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.WorkoutSession
import java.time.Duration

/** Headline numbers for the Progress screen, computed over the whole cached history. */
data class ProgressSummary(
    val sessionCount: Int = 0,
    val timeTrained: Duration = Duration.ZERO,
    val activeWeeks: Int = 0,
    val trackedWeeks: Int = DEFAULT_WEEKS,
    val volumeTrend: Double? = null,
)

/**
 * Set counts barely move against a fixed plan, so the summary leans on what does: how often the
 * client trained, for how long, and where the volume went between the first workout and the last.
 *
 * Kept out of the composable so the arithmetic can be tested without rendering anything.
 */
fun progressSummary(sessions: List<WorkoutSession>, chart: List<WeeklyVolume>): ProgressSummary {
    val lifted = sessions.sortedBy { it.startedAt }.filter { it.totalVolume > 0.0 }

    return ProgressSummary(
        sessionCount = sessions.size,
        timeTrained = sessions.fold(Duration.ZERO) { total, session -> total + session.elapsed() },
        activeWeeks = chart.count { it.volume > 0.0 },
        trackedWeeks = if (chart.isEmpty()) DEFAULT_WEEKS else chart.size,
        volumeTrend = if (lifted.size < 2) {
            null
        } else {
            lifted.last().totalVolume / lifted.first().totalVolume - 1.0
        },
    )
}

/**
 * Volume change against the previous workout, keyed by [WorkoutSession.localId]. A session with no
 * comparable predecessor is absent rather than zero: "no change" and "nothing to compare" are
 * different claims.
 */
fun volumeDeltas(sessions: List<WorkoutSession>): Map<Long, Double> =
    sessions
        .sortedBy { it.startedAt }
        .filter { it.totalVolume > 0.0 }
        .zipWithNext { previous, current -> current.localId to (current.totalVolume / previous.totalVolume - 1.0) }
        .toMap()

/** A session still in progress has no end yet, so it contributes nothing to the time trained. */
fun WorkoutSession.elapsed(): Duration =
    completedAt?.let { Duration.between(startedAt, it) } ?: Duration.ZERO
