package it.bailettitommaso.allena.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.bailettitommaso.allena.data.repository.AuthRepositoryImpl
import it.bailettitommaso.allena.data.repository.ExerciseRepositoryImpl
import it.bailettitommaso.allena.data.repository.MeRepositoryImpl
import it.bailettitommaso.allena.data.repository.SessionRepositoryImpl
import it.bailettitommaso.allena.data.repository.WorkoutRepositoryImpl
import it.bailettitommaso.allena.domain.repository.AuthRepository
import it.bailettitommaso.allena.domain.repository.ExerciseRepository
import it.bailettitommaso.allena.domain.repository.MeRepository
import it.bailettitommaso.allena.domain.repository.SessionRepository
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindMeRepository(impl: MeRepositoryImpl): MeRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository
}
