package it.bailettitommaso.allena.di

import it.bailettitommaso.allena.BuildConfig
import it.bailettitommaso.allena.data.local.TokenStore
import it.bailettitommaso.allena.data.remote.api.AuthApi
import it.bailettitommaso.allena.data.remote.api.ExerciseApi
import it.bailettitommaso.allena.data.remote.api.MeApi
import it.bailettitommaso.allena.data.remote.api.WorkoutApi
import it.bailettitommaso.allena.data.remote.interceptor.AcceptJsonInterceptor
import it.bailettitommaso.allena.data.remote.interceptor.BearerTokenInterceptor
import it.bailettitommaso.allena.data.remote.interceptor.UnauthorizedInterceptor
import it.bailettitommaso.allena.data.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenStore: TokenStore, sessionManager: SessionManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(BearerTokenInterceptor(tokenStore))
            .addInterceptor(AcceptJsonInterceptor())
            .addInterceptor(UnauthorizedInterceptor(sessionManager))
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMeApi(retrofit: Retrofit): MeApi = retrofit.create(MeApi::class.java)

    @Provides
    @Singleton
    fun provideExerciseApi(retrofit: Retrofit): ExerciseApi = retrofit.create(ExerciseApi::class.java)

    @Provides
    @Singleton
    fun provideWorkoutApi(retrofit: Retrofit): WorkoutApi = retrofit.create(WorkoutApi::class.java)
}
