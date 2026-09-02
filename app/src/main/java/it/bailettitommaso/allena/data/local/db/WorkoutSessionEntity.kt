package it.bailettitommaso.allena.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Written before the workout starts and updated as it runs, so a session survives
 * the app dying mid-workout. [isPending] means it still owes the server an upload.
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val remoteId: Long?,
    val planId: Long,
    val planName: String?,
    val startedAtMillis: Long,
    val completedAtMillis: Long?,
    val notes: String?,
    val setCount: Int,
    val totalVolume: Double,
    val isPending: Boolean,
)
