package com.example.testbioprocessor.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:8000"

    private fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
    }

    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .method(original.method, original.body)
                logRequest(original)
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .build()
    }

    private fun logRequest(request: Request) {
        println("🔵 === RETROFIT REQUEST ===")
        println("🔵 URL: ${request.url}")
        println("🔵 Method: ${request.method}")
        println("🔵 Headers: ${request.headers}")

        request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                val requestBody = buffer.readUtf8()

                println("🔵 Body Length: ${requestBody.length} characters")

                // Для JSON показываем красиво
                if (requestBody.startsWith("{") && requestBody.endsWith("}")) {
                    println("🔵 JSON Body:")
                    println(requestBody)
                } else {
                    println("🔵 Body: $requestBody")
                }

            } catch (e: Exception) {
                println("🔵 Error reading body: ${e.message}")
            }
        }
        println("🔵 =========================")
    }


}