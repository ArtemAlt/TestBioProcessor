package com.example.testbioprocessor.model

data class BaseResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val isSuccessful: Boolean = false
)