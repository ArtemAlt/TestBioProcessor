package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class HealthResponse(
    @SerializedName("status")
    val status: HealthRecognitionStatus,
)