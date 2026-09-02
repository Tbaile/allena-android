package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ProgressSummaryTest {

    private val utc = ZoneId.of("UTC")

    // Thursday.
    private val today = LocalDate.of(2026, 8, 27)

    private fun session(
        localId: Long,
        startedAt: String,
        volume: Double,
        minutes: Long? = 50,
    ) = WorkoutSession(
        localId = localId,
        remoteId = localId,
        planId = 1,
        planName = "Full Body A",
        startedAt = Instant.parse(startedAt),
        completedAt = minutes?.let { Instant.parse(startedAt).plus(Duration.ofMinutes(it)) },
        notes = null,
        setCount = 20,
        totalVolume = volume,
        isPending = false,
    )

    private fun summaryOf(sessions: List<WorkoutSession>) =
        progressSummary(sessions, weeklyVolume(sessions, today = today, zone = utc))

    @Test
    fun `counts sessions and sums the time spent training`() {
        val summary = summaryOf(
            listOf(
                session(1, "2026-08-10T18:00:00Z", 5000.0, minutes = 52),
                session(2, "2026-08-24T18:00:00Z", 6000.0, minutes = 40),
            ),
        )

        assertEquals(2, summary.sessionCount)
        assertEquals(Duration.ofMinutes(92), summary.timeTrained)
    }

    @Test
    fun `a session still in progress adds nothing to the time trained`() {
        val summary = summaryOf(listOf(session(1, "2026-08-24T18:00:00Z", 5000.0, minutes = null)))

        assertEquals(1, summary.sessionCount)
        assertEquals(Duration.ZERO, summary.timeTrained)
    }

    @Test
    fun `active weeks counts the charted weeks that have volume`() {
        val summary = summaryOf(
            listOf(
                session(1, "2026-07-13T18:00:00Z", 4000.0),
                session(2, "2026-08-17T18:00:00Z", 5000.0),
                session(3, "2026-08-19T18:00:00Z", 5100.0),
            ),
        )

        // Two distinct weeks trained out of the eight charted; the two August sessions share one.
        assertEquals(2, summary.activeWeeks)
        assertEquals(DEFAULT_WEEKS, summary.trackedWeeks)
    }

    @Test
    fun `the trend compares the oldest workout with the newest`() {
        val summary = summaryOf(
            listOf(
                session(2, "2026-08-24T18:00:00Z", 7905.0),
                session(1, "2026-07-13T18:00:00Z", 5420.0),
            ),
        )

        assertEquals(0.4585, summary.volumeTrend!!, 0.0001)
    }

    @Test
    fun `a single workout has nothing to trend against`() {
        assertNull(summaryOf(listOf(session(1, "2026-08-24T18:00:00Z", 5000.0))).volumeTrend)
    }

    @Test
    fun `an empty history summarises to zero`() {
        val summary = summaryOf(emptyList())

        assertEquals(0, summary.sessionCount)
        assertEquals(Duration.ZERO, summary.timeTrained)
        assertEquals(0, summary.activeWeeks)
        assertNull(summary.volumeTrend)
    }

    @Test
    fun `each session is compared with the one before it, oldest excluded`() {
        val deltas = volumeDeltas(
            listOf(
                session(3, "2026-08-24T18:00:00Z", 6000.0),
                session(2, "2026-08-17T18:00:00Z", 5000.0),
                session(1, "2026-08-10T18:00:00Z", 4000.0),
            ),
        )

        assertEquals(0.25, deltas[2]!!, 0.0001)
        assertEquals(0.2, deltas[3]!!, 0.0001)
        assertTrue(deltas[1] == null)
    }

    @Test
    fun `a workout without volume is skipped rather than dividing by zero`() {
        val deltas = volumeDeltas(
            listOf(
                session(1, "2026-08-10T18:00:00Z", 4000.0),
                session(2, "2026-08-17T18:00:00Z", 0.0),
                session(3, "2026-08-24T18:00:00Z", 5000.0),
            ),
        )

        assertNull(deltas[2])
        assertEquals(0.25, deltas[3]!!, 0.0001)
    }
}
