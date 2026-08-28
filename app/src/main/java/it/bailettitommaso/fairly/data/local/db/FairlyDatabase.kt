package it.bailettitommaso.fairly.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        FavoriteExerciseEntity::class,
        WorkoutPlanEntity::class,
        WorkoutPlanItemEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetLogEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class FairlyDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun favoriteExerciseDao(): FavoriteExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
}
