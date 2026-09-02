package it.bailettitommaso.allena.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
)
