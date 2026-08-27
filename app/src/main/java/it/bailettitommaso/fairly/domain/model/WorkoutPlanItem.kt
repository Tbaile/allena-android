package it.bailettitommaso.fairly.domain.model

data class WorkoutPlanItem(
    val id: Long,
    val position: Int,
    val sets: Int,
    val reps: Int?,
    val durationSeconds: Int?,
    val restSeconds: Int,
    val targetWeight: Double?,
    val notes: String?,
    val exercise: Exercise,
) {
    /** A timed hold prescribes a duration instead of a rep count. */
    val isTimed: Boolean get() = reps == null && durationSeconds != null
}
