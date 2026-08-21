package it.bailettitommaso.fairly.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_exercises")
data class FavoriteExerciseEntity(
    @PrimaryKey val exerciseId: Long,
)
