package com.example.testbioprocessor.viewModel

import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbioprocessor.App
import com.example.testbioprocessor.api.BioApi
import com.example.testbioprocessor.login.UserPreferencesNew
import com.example.testbioprocessor.login.UserState
import com.example.testbioprocessor.login.UserVectorState
import com.example.testbioprocessor.model.DeleteResponse
import com.example.testbioprocessor.model.DeleteResponseStatus
import com.example.testbioprocessor.model.HealthRecognitionStatus
import com.example.testbioprocessor.model.HealthResponse
import com.example.testbioprocessor.model.RecognitionRequest
import com.example.testbioprocessor.model.RecognitionResponse
import com.example.testbioprocessor.model.RecognitionStatus
import com.example.testbioprocessor.model.RegisterRequest
import com.example.testbioprocessor.model.RegisterResponse
import com.example.testbioprocessor.model.RegisterResponseStatus
import com.example.testbioprocessor.model.camera.CapturedImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.Locale

class BioViewModelNew() : ViewModel() {

    private val api: BioApi = NetworkModule.createApi(App.instance, BioApi::class.java)
    private val _uiLoginState = MutableStateFlow(UserState())
    val uiLoginState = _uiLoginState.asStateFlow()
    private val userPreferences: UserPreferencesNew = UserPreferencesNew(App.instance)
    private val _imagesState = MutableStateFlow<List<CapturedImage>>(emptyList())
    val imagesState = _imagesState.asStateFlow()
    private val _uiHealthCheckState = MutableStateFlow(HealthRecognitionStatus.NO_HEALTHY)
    val uiHealthCheckState = _uiHealthCheckState.asStateFlow()
    private var _uiApiState = MutableStateFlow<ApiUiState>(ApiUiState.Idle)
    val uiApiState = _uiApiState.asStateFlow()

    init {
        loadSavedUser()
//        userPreferences.drop()
    }

    private fun loadSavedUser() {
        viewModelScope.launch {
            val savedState = userPreferences.getUserState()
            _uiLoginState.update { oldState ->
                oldState.copy(
                    login = savedState.login,
                    isLoginSaved = savedState.isLoginSaved,
                    vectorSaved = savedState.vectorSaved
                )
            }
        }
    }

    fun checkHealth() {
        viewModelScope.launch {
            val result = runCatching {
                delay(2000L)
                api.healthCheck()
            }.getOrElse {
                HealthResponse(HealthRecognitionStatus.NO_HEALTHY)
            }
            _uiHealthCheckState.update { result.status }
        }
    }


    fun registerBioVector() {
        val images = _imagesState.value.map { it.toBase64() }
        val name = _uiLoginState.value.login
        viewModelScope.launch {
            _uiApiState.update { ApiUiState.Loading }
            val result =
                runCatching { api.registerPerson(RegisterRequest(name, images)) }
                    .getOrElse {
                        RegisterResponse(
                            RegisterResponseStatus.ERROR,
                            "Ошибка регистрации"
                        )
                    }
            if (result.status == RegisterResponseStatus.SUCCESS) {
                updateVectorState(true)
                _uiApiState.update { ApiUiState.Success(result.message) }
            } else {
                _uiApiState.update { ApiUiState.Error(result.message) }
            }
            _imagesState.value = emptyList()
        }

    }

    fun registerBioVectorMultipart() {
        val images = _imagesState.value
        val name = _uiLoginState.value.login

        viewModelScope.launch {
            _uiApiState.update { ApiUiState.Loading }

            val result = try {
                if (images.isEmpty()) {
                    ApiUiState.Error("Нет фотографий для регистрации")
                } else {
                    // Используем multipart загрузку
                    registerWithMultipart(images, name)
                }
            } catch (e: Exception) {
                ApiUiState.Error("Ошибка регистрации: ${e.message}")
            }

            _uiApiState.update { result }
            _imagesState.value = emptyList()
        }
    }

    private suspend fun registerWithMultipart(images: List<CapturedImage>, name: String): ApiUiState {
        // Создаем части для multipart запроса
        val namePart = createNamePart(name)
        val imageParts = createImageParts(images)

        // Отправляем multipart запрос
        val response = api.registerPersonMultipart(namePart, imageParts)

        return if (response.status == RegisterResponseStatus.SUCCESS) {
            updateVectorState(true)
            ApiUiState.Success(response.message)
        } else {
            ApiUiState.Error(response.message)
        }
    }

    // Вспомогательные функции для создания multipart частей
    private fun createNamePart(name: String): RequestBody {
        return RequestBody.create("text/plain".toMediaType(), name)
    }

    private fun createImageParts(images: List<CapturedImage>): List<MultipartBody.Part> {
        return images.mapIndexed { index, capturedImage ->
            // Сжимаем изображение
            val compressedImage = compressImage(capturedImage.bitmap)
            val requestBody = RequestBody.create("image/jpeg".toMediaType(), compressedImage)

            MultipartBody.Part.createFormData(
                "images",
                "photo_${index + 1}.jpg",
                requestBody
            )
        }
    }

    private fun compressImage(bitmap: Bitmap, maxQuality: Int = 60, maxSizeKB: Int = 200): ByteArray {
        var quality = maxQuality
        var outputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var byteArray = outputStream.toByteArray()

        // Агрессивное сжатие для 3 фото
        while (byteArray.size > maxSizeKB * 1024 && quality > 20) {
            quality -= 15
            outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            byteArray = outputStream.toByteArray()
            Log.d("ImageCompression", "Compressing: quality=$quality%, size=${byteArray.size / 1024}KB")
        }

        Log.d("ImageCompression", "Final: ${byteArray.size / 1024}KB, quality: $quality%")
        return byteArray
    }

    private fun updateVectorState(isSaved: Boolean) {
        val currentTime =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val currentUser = _uiLoginState.value.login
        val newVectorState = UserVectorState(
            isSaved = isSaved,
            data = if (isSaved) currentTime else ""
        )
        val newState = _uiLoginState.value.copy(
            isLoginSaved = isSaved,
            login = if (isSaved) currentUser else "",
            vectorSaved = newVectorState
        )
        _uiLoginState.value = newState
        userPreferences.saveUserState(newState)
    }

    fun recognizePerson() {
        val image = _imagesState.value.map { it.toBase64() }.first()
        viewModelScope.launch {
            _uiApiState.update { ApiUiState.Loading }
            val result = runCatching { api.recognizePerson(RecognitionRequest(image)) }
                .getOrElse {
                    RecognitionResponse(
                        status = RecognitionStatus.ERROR,
                        name = "",
                        similarity = "",
                        error = "error"
                    )
                }
            val currentStatus = when (result.status) {
                RecognitionStatus.SUCCESS -> ApiUiState.RecognitionSuccess(
                    result.name!!,
                    result.similarity!!
                )

                RecognitionStatus.MULTIPLE_FACES -> ApiUiState.Error("Множественные лица на фото. Замените фотографию")
                RecognitionStatus.NO_FACES -> ApiUiState.Error("Не определил на фотографии лицо. Замените фотографию")
                RecognitionStatus.NOT_REGISTERED -> ApiUiState.Error("Не узнал Вас")
                else -> ApiUiState.Error("Unknown status")
            }
            _uiApiState.update { currentStatus }
        }
    }

    fun setImages(capturedImages: List<CapturedImage>) {
        _imagesState.update { images ->
            images + capturedImages
        }
    }

    fun clearImagesState() {
        _imagesState.value = emptyList()
    }

    fun saveLogin(login: String) {
        viewModelScope.launch {
            userPreferences.saveLogin(login)
            _uiLoginState.update { it.copy(login = login, isLoginSaved = true) }
        }
    }

    fun resetLoginAnVector() {
        val currentLogin = _uiLoginState.value.login
        val isVector = _uiLoginState.value.vectorSaved.isSaved
        viewModelScope.launch {
            _uiApiState.update { ApiUiState.Loading }
            var result = DeleteResponse(
                DeleteResponseStatus.SUCCESS,
                "Удаление без вектора"
            )
            if (isVector) {
                result =
                    runCatching { api.deleteVector(currentLogin) }
                        .getOrElse {
                            DeleteResponse(
                                DeleteResponseStatus.ERROR,
                                "Ошибка удаления ветора"
                            )
                        }
            }

            if (result.status == DeleteResponseStatus.SUCCESS) {
                updateVectorState(false)
                _uiApiState.update { ApiUiState.Success(result.message) }
            } else {
                _uiApiState.update { ApiUiState.Error(result.message) }
            }
        }
    }

    fun resetApiState() {
        _uiApiState.value = ApiUiState.Idle
    }

}

sealed class ApiUiState {
    object Idle : ApiUiState()
    object Loading : ApiUiState()
    data class Success(val message: String) : ApiUiState()
    data class RecognitionSuccess(val name: String, val similarity: String) : ApiUiState()
    data class RegistrationSuccess(val name: String) : ApiUiState()
    data class Error(val message: String) : ApiUiState()
}





