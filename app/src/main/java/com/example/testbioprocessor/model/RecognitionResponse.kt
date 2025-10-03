package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class RecognitionResponse(
    @SerializedName("status")
    var status: RecognitionStatus,

    @SerializedName("name")
    val name: String?,

    @SerializedName("similarity")
    val similarity: Float?,

    @SerializedName("error")
    val error: String?
)
