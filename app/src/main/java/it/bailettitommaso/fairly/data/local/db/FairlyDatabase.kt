package it.bailettitommaso.fairly.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ExerciseEntity::class, FavoriteExerciseEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class FairlyDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun favoriteExerciseDao(): FavoriteExerciseDao
}
