package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("status")
    var status: RegisterResponseStatus,

    @SerializedName("message")
    val message: String,

    )
