package com.example.testbioprocessor.model.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.pow

data class CapturedImage(
    val uri: Uri,
    val file: File,
    val index: Int,
) {
    val bitmap: Bitmap by lazy { createBitMap() }
    private fun createBitMap(): Bitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = 2 // Уменьшаем в 2 раза
            inPreferredConfig = Bitmap.Config.RGB_565 // Экономим память
        }

        return BitmapFactory.decodeFile(file.path, options)
    }

    fun toBase64() = compressImage(file)

    private fun compressImage(imageFile: File, maxFileSizeKB: Int = 500): String {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imageFile.absolutePath, options)

            // Вычисляем коэффициент сжатия
            var scale = 1
            while ((options.outWidth * options.outHeight) * (1 / scale.toDouble().pow(2)) > maxFileSizeKB * 1024) {
                scale++
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = scale

            val decodeFile = BitmapFactory.decodeFile(imageFile.absolutePath, options)
                ?: throw Exception("Не удалось декодировать изображение")

            val outputStream = ByteArrayOutputStream()
            decodeFile.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            // Используем NO_WRAP чтобы убрать символы \n
            Base64.encodeToString(byteArray, Base64.NO_WRAP)

        } catch (e: Exception) {
            println("❌ Ошибка сжатия изображения: ${e.message}")
            // Fallback: оригинальное изображение без переносов
            val originalBytes = imageFile.readBytes()
            Base64.encodeToString(originalBytes, Base64.NO_WRAP)
        }
    }
}




