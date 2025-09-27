package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("images")
    val images: List<String> // List of base64 strings
)