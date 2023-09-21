package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
    }

    @Singleton
    @Provides
    fun provideLogging(): HttpLoggingInterceptor =  HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = appAuth.data.value?.token?.let { token ->
                chain.request()
                    .newBuilder()
                    .addHeader("Authorization", token)
                    .build()
            } ?: chain.request()

            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    @Singleton
    @Provides
    fun provideAuthApiService(
        retrofit: Retrofit
    ): AuthApiService = retrofit.create()

    @Singleton
    @Provides
    fun providePostApiService(
        retrofit: Retrofit
    ): PostApiService = retrofit.create()
}
