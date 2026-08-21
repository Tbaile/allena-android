package it.bailettitommaso.fairly.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExerciseEntity::class], version = 1, exportSchema = false)
abstract class FairlyDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}
