package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

data class DeleteResponse (
    @SerializedName("status")
    var status: DeleteResponseStatus,

    @SerializedName("message")
    val message: String,
)
