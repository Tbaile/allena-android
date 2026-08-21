package it.bailettitommaso.fairly.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.bailettitommaso.fairly.data.local.db.ExerciseDao
import it.bailettitommaso.fairly.data.local.db.FairlyDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FairlyDatabase =
        Room.databaseBuilder(context, FairlyDatabase::class.java, "fairly.db").build()

    @Provides
    fun provideExerciseDao(database: FairlyDatabase): ExerciseDao = database.exerciseDao()
}
