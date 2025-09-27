package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class ValidationError(
    @SerializedName("loc")
    val location: List<Any>,

    @SerializedName("msg")
    val message: String,

    @SerializedName("type")
    val type: String
)
