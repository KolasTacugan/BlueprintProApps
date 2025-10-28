package com.example.blueprintproapps.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Emulator localhost URL
    private const val BASE_URL = "http://10.0.2.2:5169/"

    // Logging interceptor for debugging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client with timeouts
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)   // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)      // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)     // Write timeout
        .build()

    // Retrofit instance
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }


}
