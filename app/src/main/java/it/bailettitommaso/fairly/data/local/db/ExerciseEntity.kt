package it.bailettitommaso.fairly.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val categoryId: Long?,
    val categoryName: String?,
    val categorySlug: String?,
    val tagsJson: String,
    val videoUrl: String?,
)
