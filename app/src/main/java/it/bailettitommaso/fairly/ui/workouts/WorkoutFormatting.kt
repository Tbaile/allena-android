package it.bailettitommaso.fairly.ui.workouts

import it.bailettitommaso.fairly.domain.model.WorkoutPlanItem

/** "4 × 8" for a rep target, "3 × 45s" for a timed hold. */
fun WorkoutPlanItem.loadLabel(): String {
    val perSet = if (isTimed) "${durationSeconds}s" else reps?.toString() ?: "—"
    return "$sets × $perSet"
}

/** Rest between sets as "45s", "1:30" or "2:00". */
fun formatRest(seconds: Int): String =
    if (seconds < 60) "${seconds}s" else "%d:%02d".format(seconds / 60, seconds % 60)

fun formatWeight(kilograms: Double): String =
    if (kilograms % 1.0 == 0.0) "${kilograms.toInt()} kg" else "$kilograms kg"
