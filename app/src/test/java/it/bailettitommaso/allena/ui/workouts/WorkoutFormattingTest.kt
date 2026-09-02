package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.WorkoutPlanItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class WorkoutFormattingTest {

    private fun item(sets: Int, reps: Int?, durationSeconds: Int?) = WorkoutPlanItem(
        id = 1,
        position = 1,
        sets = sets,
        reps = reps,
        durationSeconds = durationSeconds,
        restSeconds = 60,
        targetWeight = null,
        notes = null,
        exercise = Exercise(1, "Plank Hold", "", null, null),
    )

    @Test
    fun `a rep target reads as sets by reps`() {
        assertEquals("4 × 8", item(sets = 4, reps = 8, durationSeconds = null).loadLabel())
    }

    @Test
    fun `a timed hold reads as sets by seconds`() {
        assertEquals("3 × 45s", item(sets = 3, reps = null, durationSeconds = 45).loadLabel())
    }

    @Test
    fun `rest under a minute stays in seconds`() {
        assertEquals("45s", formatRest(45))
    }

    @Test
    fun `rest of a minute or more reads as minutes and seconds`() {
        assertEquals("1:30", formatRest(90))
        assertEquals("2:00", formatRest(120))
        assertEquals("1:00", formatRest(60))
    }

    @Test
    fun `whole weights drop the decimal`() {
        assertEquals("60 kg", formatWeight(60.0))
        assertEquals("2.5 kg", formatWeight(2.5))
    }

    @Test
    fun `volume is grouped for readability`() {
        assertEquals("7,905 kg", formatVolume(7905.0))
        assertEquals("0 kg", formatVolume(0.0))
    }

    @Test
    fun `under an hour reads in minutes`() {
        assertEquals("52 min", formatDuration(Duration.ofMinutes(52)))
        assertEquals("0 min", formatDuration(Duration.ZERO))
    }

    @Test
    fun `an hour or more reads as hours and padded minutes`() {
        assertEquals("5h 12m", formatDuration(Duration.ofMinutes(312)))
        assertEquals("1h 00m", formatDuration(Duration.ofMinutes(60)))
        assertEquals("2h 05m", formatDuration(Duration.ofMinutes(125)))
    }

    @Test
    fun `small changes keep a decimal, large ones round`() {
        assertEquals("+4.7%", formatPercentDelta(0.047))
        assertEquals("+46%", formatPercentDelta(0.4585))
    }

    @Test
    fun `a drop is signed with a minus`() {
        assertEquals("\u22123.2%", formatPercentDelta(-0.032))
        assertEquals("+0.0%", formatPercentDelta(0.0))
    }
}
