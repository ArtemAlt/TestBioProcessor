package com.example.testbioprocessor.viewModel

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbioprocessor.App
import com.example.testbioprocessor.api.BioApi
import com.example.testbioprocessor.api.NetworkModule
import com.example.testbioprocessor.login.UserPreferencesNew
import com.example.testbioprocessor.login.UserState
import com.example.testbioprocessor.login.UserVectorState
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
import java.util.Date
import java.util.Locale

class BioViewModelNew() : ViewModel() {

    private val api: BioApi = NetworkModule.provideRetrofit().create(BioApi::class.java)
    private val _uiLoginState = MutableStateFlow(UserState())
    val uiLoginState = _uiLoginState.asStateFlow()
    private val userPreferences: UserPreferencesNew = UserPreferencesNew(App.instance)
    private val _imagesState = MutableStateFlow<List<CapturedImage>>(emptyList())
    val imagesState = _imagesState.asStateFlow()
    private val _uiHealthCheckState = MutableStateFlow(HealthRecognitionStatus.NO_HEALTHY)
    val uiHealthCheckState = _uiHealthCheckState.asStateFlow()
    private val _uiApiState = MutableStateFlow<ApiUiState>(ApiUiState.Idle)
    val uiApiState = _uiApiState.asStateFlow()

    init {
        loadSavedUser()
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

    private fun dropUserinfo() {
        viewModelScope.launch {
            val savedState = userPreferences.getUserState()
            _uiLoginState.update { oldState ->
                oldState.copy(
                    login = "",
                    isLoginSaved = false,
                    vectorSaved = UserVectorState()
                )
            }
        }
    }



    private fun updateVectorState(isSaved: Boolean) {
        val currentTime =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val newVectorState = UserVectorState(
            isSaved = isSaved,
            data = currentTime
        )
        val newState = _uiLoginState.value.copy(
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
                        similarity = 0.0f,
                        error = "error"
                    )
                }
            val currentStatus = when (result.status) {
                RecognitionStatus.SUCCESS ->  ApiUiState.Success(result.name + result.similarity)
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

    fun saveLogin(trim: String) {
        viewModelScope.launch {
            val login = _uiLoginState.value.login.trim()
            if (login.isNotEmpty()) {
                userPreferences.getUserState().login = login
                _uiLoginState.value = _uiLoginState.value.copy(
                    isLoginSaved = true,
                )
            }
        }
    }

    fun onLoginChange(it: String) {

    }

    fun resetLogin() {
        viewModelScope.launch {
            userPreferences.getUserState().login = ""
            var v = userPreferences.getUserState().vectorSaved
            _uiLoginState.value = UserState(
                login = "",
                isLoginSaved = false,
                vectorSaved = v
            )
        }
    }

    fun getSavedLogin() = userPreferences.getLogin()

}

sealed class ApiUiState {
    object Idle : ApiUiState()
    object Loading : ApiUiState()
    data class Success(val message: String) : ApiUiState()
    data class RecognitionSuccess(val name: String, val similarity: Float) : ApiUiState()
    data class Error(val message: String) : ApiUiState()
}





