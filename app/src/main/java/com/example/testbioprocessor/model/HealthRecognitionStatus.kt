package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

enum class HealthRecognitionStatus {
    @SerializedName("healthy")
    HEALTHY,
    @SerializedName("no_healthy")
    NO_HEALTHY,
}
