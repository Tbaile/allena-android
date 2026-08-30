package it.bailettitommaso.fairly.ui.workouts

import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.model.WorkoutPlan
import it.bailettitommaso.fairly.domain.model.WorkoutPlanItem

private val strength = Category(3, "Strength", "strength")

private fun exercise(id: Long, name: String) =
    Exercise(id = id, name = name, description = "", category = strength, videoUrl = null)

/** Mirrors the seeded "Full Body A" plan so previews match what the demo actually shows. */
internal val previewPlan = WorkoutPlan(
    id = 1,
    name = "Full Body A",
    description = "Lower-body power and strength paired with upper-body pressing and pulling. " +
        "Warm up for ten minutes before the first working set.",
    isActive = true,
    items = listOf(
        WorkoutPlanItem(
            id = 1, position = 1, sets = 4, reps = 5, durationSeconds = null, restSeconds = 120,
            targetWeight = 10.0, notes = "Hold a 10 kg plate at the chest. Land soft, reset between reps.",
            exercise = exercise(1, "Jump Squat"),
        ),
        WorkoutPlanItem(
            id = 2, position = 2, sets = 4, reps = 8, durationSeconds = null, restSeconds = 120,
            targetWeight = 60.0, notes = null, exercise = exercise(2, "Barbell Back Squat"),
        ),
        WorkoutPlanItem(
            id = 3, position = 3, sets = 3, reps = 8, durationSeconds = null, restSeconds = 90,
            targetWeight = 20.0, notes = "Smith machine, bench set to 43 degrees.",
            exercise = exercise(3, "Incline Bench Press"),
        ),
        WorkoutPlanItem(
            id = 4, position = 4, sets = 3, reps = null, durationSeconds = 45, restSeconds = 60,
            targetWeight = null, notes = "Timed hold. Stop the set if the hips drop.",
            exercise = exercise(4, "Plank Hold"),
        ),
    ),
)
