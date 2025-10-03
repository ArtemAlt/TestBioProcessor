package com.example.testbioprocessor.model

import com.google.gson.annotations.SerializedName

enum class RecognitionStatus {
    @SerializedName("success")
    SUCCESS,

    @SerializedName("multiple_faces")
    MULTIPLE_FACES,

    @SerializedName("no_faces")
    NO_FACES,

    @SerializedName("not_registered")
    NOT_REGISTERED,

    @SerializedName("error")
    ERROR,
}
