package com.example.testbioprocessor.api

import com.example.testbioprocessor.model.DeleteResponse
import com.example.testbioprocessor.model.HealthResponse
import com.example.testbioprocessor.model.RecognitionRequest
import com.example.testbioprocessor.model.RecognitionResponse
import com.example.testbioprocessor.model.RegisterRequest
import com.example.testbioprocessor.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BioApi {
    // Health check
    @GET("/health")
    suspend fun healthCheck(): HealthResponse

    // Register person
    @POST("/register")
    suspend fun registerPerson(
        @Body request: RegisterRequest
    ): RegisterResponse

    // Recognize person
    @POST("/recognize")
    suspend fun recognizePerson(
        @Body request: RecognitionRequest
    ): RecognitionResponse

    @DELETE("/persons/{name}")
    suspend fun deleteVector(@Path("name") name: String): DeleteResponse
}
