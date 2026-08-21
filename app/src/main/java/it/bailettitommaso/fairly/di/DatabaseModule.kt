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
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS favorite_exercises (exerciseId INTEGER NOT NULL PRIMARY KEY)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FairlyDatabase =
        Room.databaseBuilder(context, FairlyDatabase::class.java, "fairly.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideExerciseDao(database: FairlyDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideFavoriteExerciseDao(database: FairlyDatabase): FavoriteExerciseDao = database.favoriteExerciseDao()
}
