package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

enum class DeleteResponseStatus {
    @SerializedName("success")
    SUCCESS,
    @SerializedName("error")
    ERROR,
}
