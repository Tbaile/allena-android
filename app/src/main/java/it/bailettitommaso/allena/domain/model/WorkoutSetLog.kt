package it.bailettitommaso.allena.domain.model

data class WorkoutSetLog(
    val id: Long,
    val planItemId: Long,
    val setNumber: Int,
    val reps: Int?,
    val weight: Double?,
    val durationSeconds: Int?,
)
