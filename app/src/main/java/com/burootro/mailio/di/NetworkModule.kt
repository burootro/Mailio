package com.burootro.mailio.di

import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.remote.MailioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://mailio-backend.onrender.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(prefs: MailioPreferences): OkHttpClient {

        // بيضيف مفتاح الاسترجاع ومعرّف الجهاز لكل طلب تلقائياً
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()

            val key = runBlocking { prefs.getRecoveryKey() }
            val deviceId = runBlocking { prefs.getOrCreateDeviceId() }

            val builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("X-Device-Id", deviceId)

            if (key != null) {
                builder.header("Authorization", "Bearer $key")
            }

            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            // السيرفر المجاني بينام، فأول طلب ممكن ياخد وقت
            .connectTimeout(70, TimeUnit.SECONDS)
            .readTimeout(70, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()

    @Provides
    @Singleton
    fun provideMailioApi(retrofit: Retrofit): MailioApi =
        retrofit.create(MailioApi::class.java)
}
