package it.bailettitommaso.fairly.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_set_logs",
    indices = [Index("sessionLocalId")],
)
data class WorkoutSetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionLocalId: Long,
    val planItemId: Long,
    val setNumber: Int,
    val reps: Int?,
    val weight: Double?,
    val durationSeconds: Int?,
)
