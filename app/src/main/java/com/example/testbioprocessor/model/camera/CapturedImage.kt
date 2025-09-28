package com.example.testbioprocessor.model.camera

import android.net.Uri
import android.util.Base64
import java.io.File

data class CapturedImage(
    val uri: Uri,
    val file: File,
    val index: Int
) {
    fun toBase64(): String {
        return file.inputStream().use { inputStream ->
            val bytes = inputStream.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        }

    }
}


