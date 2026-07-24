package it.bailettitommaso.fairly.di

import javax.inject.Qualifier

/** Marks the app-lifetime [kotlinx.coroutines.CoroutineScope] used for fire-and-forget work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
