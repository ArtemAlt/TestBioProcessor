package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class RecognitionRequest(
    @SerializedName("image")
    val image: String // base64 string
)