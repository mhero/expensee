package com.mac.expensee.core.network.di

import com.mac.expensee.core.network.BuildConfig
import com.mac.expensee.core.network.api.AuthApi
import com.mac.expensee.core.network.api.ExpenseApi
import com.mac.expensee.core.network.config.ApiConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import org.koin.dsl.module
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {

    single { ApiConfig(baseUrl = BuildConfig.API_BASE_URL) }

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single {
        val json: Json = get()
        Retrofit.Builder()
            .baseUrl(get<ApiConfig>().baseUrl)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>().create(ExpenseApi::class.java) }
    single { get<Retrofit>().create(AuthApi::class.java) }
}
