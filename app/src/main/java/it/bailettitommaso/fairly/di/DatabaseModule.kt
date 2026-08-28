package it.bailettitommaso.fairly.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.bailettitommaso.fairly.data.local.db.ExerciseDao
import it.bailettitommaso.fairly.data.local.db.FairlyDatabase
import it.bailettitommaso.fairly.data.local.db.FavoriteExerciseDao
import it.bailettitommaso.fairly.data.local.db.WorkoutPlanDao
import it.bailettitommaso.fairly.data.local.db.WorkoutSessionDao
import javax.inject.Singleton

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS favorite_exercises (exerciseId INTEGER NOT NULL PRIMARY KEY)")
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS workout_plans (" +
                "id INTEGER NOT NULL PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "isActive INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS workout_plan_items (" +
                "id INTEGER NOT NULL PRIMARY KEY, " +
                "planId INTEGER NOT NULL, " +
                "exerciseId INTEGER NOT NULL, " +
                "position INTEGER NOT NULL, " +
                "sets INTEGER NOT NULL, " +
                "reps INTEGER, " +
                "durationSeconds INTEGER, " +
                "restSeconds INTEGER NOT NULL, " +
                "targetWeight REAL, " +
                "notes TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_plan_items_planId ON workout_plan_items (planId)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS workout_sessions (" +
                "localId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "remoteId INTEGER, " +
                "planId INTEGER NOT NULL, " +
                "planName TEXT, " +
                "startedAtMillis INTEGER NOT NULL, " +
                "completedAtMillis INTEGER, " +
                "notes TEXT, " +
                "setCount INTEGER NOT NULL, " +
                "totalVolume REAL NOT NULL, " +
                "isPending INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS workout_set_logs (" +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "sessionLocalId INTEGER NOT NULL, " +
                "planItemId INTEGER NOT NULL, " +
                "setNumber INTEGER NOT NULL, " +
                "reps INTEGER, " +
                "weight REAL, " +
                "durationSeconds INTEGER)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_set_logs_sessionLocalId ON workout_set_logs (sessionLocalId)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FairlyDatabase =
        Room.databaseBuilder(context, FairlyDatabase::class.java, "fairly.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideExerciseDao(database: FairlyDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideFavoriteExerciseDao(database: FairlyDatabase): FavoriteExerciseDao = database.favoriteExerciseDao()

    @Provides
    fun provideWorkoutPlanDao(database: FairlyDatabase): WorkoutPlanDao = database.workoutPlanDao()

    @Provides
    fun provideWorkoutSessionDao(database: FairlyDatabase): WorkoutSessionDao = database.workoutSessionDao()
}
