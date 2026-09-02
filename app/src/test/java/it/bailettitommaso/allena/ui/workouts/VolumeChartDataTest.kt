package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class VolumeChartDataTest {

    private val utc = ZoneId.of("UTC")

    // Thursday.
    private val today = LocalDate.of(2026, 8, 27)

    private fun session(startedAt: String, volume: Double) = WorkoutSession(
        localId = 1,
        remoteId = null,
        planId = 1,
        planName = "Full Body A",
        startedAt = Instant.parse(startedAt),
        completedAt = Instant.parse(startedAt),
        notes = null,
        setCount = 1,
        totalVolume = volume,
        isPending = false,
    )

    @Test
    fun `returns one bucket per requested week, oldest first`() {
        val bars = weeklyVolume(emptyList(), today = today, weeks = 8, zone = utc)

        assertEquals(8, bars.size)
        assertEquals(LocalDate.of(2026, 7, 6), bars.first().weekStart)
        // The current week is last, and starts on the Monday.
        assertEquals(LocalDate.of(2026, 8, 24), bars.last().weekStart)
    }

    @Test
    fun `sessions in the same week are summed`() {
        val bars = weeklyVolume(
            listOf(
                session("2026-08-24T10:00:00Z", 1000.0),
                session("2026-08-26T10:00:00Z", 500.0),
            ),
            today = today, weeks = 8, zone = utc,
        )

        assertEquals(1500.0, bars.last().volume, 0.0)
    }

    @Test
    fun `a week without training stays in the chart as an empty bucket`() {
        val bars = weeklyVolume(
            listOf(session("2026-08-17T10:00:00Z", 900.0)),
            today = today, weeks = 8, zone = utc,
        )

        assertEquals(900.0, bars[6].volume, 0.0)
        assertEquals(0.0, bars[5].volume, 0.0)
        assertEquals(0.0, bars.last().volume, 0.0)
    }

    @Test
    fun `a Sunday session belongs to the week that started on Monday`() {
        val bars = weeklyVolume(
            listOf(session("2026-08-23T22:00:00Z", 700.0)),
            today = today, weeks = 8, zone = utc,
        )

        assertEquals(LocalDate.of(2026, 8, 17), bars[6].weekStart)
        assertEquals(700.0, bars[6].volume, 0.0)
        assertEquals(0.0, bars.last().volume, 0.0)
    }

    @Test
    fun `sessions older than the window are ignored`() {
        val bars = weeklyVolume(
            listOf(session("2026-01-05T10:00:00Z", 5000.0)),
            today = today, weeks = 8, zone = utc,
        )

        assertEquals(0.0, bars.sumOf { it.volume }, 0.0)
    }
}
