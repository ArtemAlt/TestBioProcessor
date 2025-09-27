package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class HTTPValidationError(
    @SerializedName("detail")
    val detail: List<ValidationError>?
)
