package it.bailettitommaso.allena.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The prescribed exercise itself is not duplicated here: [exerciseId] points at the
 * shared `exercises` cache, which the plan fetch tops up.
 */
@Entity(
    tableName = "workout_plan_items",
    indices = [Index("planId")],
)
data class WorkoutPlanItemEntity(
    @PrimaryKey val id: Long,
    val planId: Long,
    val exerciseId: Long,
    val position: Int,
    val sets: Int,
    val reps: Int?,
    val durationSeconds: Int?,
    val restSeconds: Int,
    val targetWeight: Double?,
    val notes: String?,
)
