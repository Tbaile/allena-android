package it.bailettitommaso.fairly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.bailettitommaso.fairly.data.repository.AuthRepositoryImpl
import it.bailettitommaso.fairly.data.repository.SessionRepositoryImpl
import it.bailettitommaso.fairly.domain.repository.AuthRepository
import it.bailettitommaso.fairly.domain.repository.SessionRepository
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
}
