package it.bailettitommaso.allena.domain.model

import java.time.Instant

/**
 * A workout the user performed. Sessions live in Room first so a workout survives
 * losing connectivity; [isPending] marks one that has not reached the server yet.
 */
data class WorkoutSession(
    val localId: Long,
    val remoteId: Long?,
    val planId: Long,
    val planName: String?,
    val startedAt: Instant,
    val completedAt: Instant?,
    val notes: String?,
    val setCount: Int,
    val totalVolume: Double,
    val isPending: Boolean,
)
