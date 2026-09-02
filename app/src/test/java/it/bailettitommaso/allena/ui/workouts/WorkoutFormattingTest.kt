package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.WorkoutPlanItem
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
