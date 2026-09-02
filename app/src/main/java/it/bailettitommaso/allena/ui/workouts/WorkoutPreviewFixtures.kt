package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.Tag
import it.bailettitommaso.allena.domain.model.WorkoutPlan
import it.bailettitommaso.allena.domain.model.WorkoutPlanItem

private val strength = Category(3, "Strength", "strength")

private val legs = Tag(1, "Legs", "legs")
private val compound = Tag(2, "Compound", "compound")
private val barbell = Tag(3, "Barbell", "barbell")
private val chest = Tag(4, "Chest", "chest")
private val core = Tag(5, "Core", "core")

private fun exercise(id: Long, name: String, description: String, tags: List<Tag>) =
    Exercise(id = id, name = name, description = description, category = strength, videoUrl = null, tags = tags)

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
            exercise = exercise(
                1,
                "Jump Squat",
                "Drop into a quarter squat and explode straight up. Absorb the landing through the " +
                    "hips and knees, then reset before the next rep.",
                listOf(legs, compound),
            ),
        ),
        WorkoutPlanItem(
            id = 2, position = 2, sets = 4, reps = 8, durationSeconds = null, restSeconds = 120,
            targetWeight = 60.0, notes = null,
            exercise = exercise(
                2,
                "Barbell Back Squat",
                "Rest the barbell on your upper back, brace your core and sit down until your " +
                    "thighs are parallel to the floor. Drive through the heels to stand back up.",
                listOf(legs, compound, barbell),
            ),
        ),
        WorkoutPlanItem(
            id = 3, position = 3, sets = 3, reps = 8, durationSeconds = null, restSeconds = 90,
            targetWeight = 20.0, notes = "Smith machine, bench set to 43 degrees.",
            exercise = exercise(
                3,
                "Incline Bench Press",
                "Lower the bar to the upper chest with the elbows tucked at about 45 degrees, then " +
                    "press back up without letting the shoulders roll forward.",
                listOf(chest, compound),
            ),
        ),
        WorkoutPlanItem(
            id = 4, position = 4, sets = 3, reps = null, durationSeconds = 45, restSeconds = 60,
            targetWeight = null, notes = "Timed hold. Stop the set if the hips drop.",
            exercise = exercise(
                4,
                "Plank Hold",
                "Support yourself on forearms and toes with a straight line from heels to head. " +
                    "Squeeze the glutes and keep the ribs down for the whole hold.",
                listOf(core),
            ),
        ),
    ),
)
