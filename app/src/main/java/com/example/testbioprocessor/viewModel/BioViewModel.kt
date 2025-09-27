package com.example.testbioprocessor.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbioprocessor.App
import com.example.testbioprocessor.api.BioApi
import com.example.testbioprocessor.api.NetworkModule
import com.example.testbioprocessor.login.LoginUiState
import com.example.testbioprocessor.login.UserPreferences
import com.example.testbioprocessor.model.RecognitionRequest
import com.example.testbioprocessor.model.RecognitionStatus
import com.example.testbioprocessor.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BioViewModel() : ViewModel() {

    private val api: BioApi = NetworkModule.provideRetrofit().create(BioApi::class.java)
    private val _uiState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    private val _uiLoginState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()
    val uiLoginState : StateFlow<LoginUiState> = _uiLoginState.asStateFlow()
    private val app: App = App()
    private val userPreferences: UserPreferences = UserPreferences(app.getAppContext())
    private val _registrationState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
//    val registrationState: StateFlow<RegistrationUiState> = _registrationState.asStateFlow()

    init {
        loadSavedLogin()
    }

    private fun loadSavedLogin() {
        viewModelScope.launch {
            val savedLogin = userPreferences.login
            _uiLoginState.value = _uiLoginState.value.copy(
                login = savedLogin.toString(),
                isLoginSaved = !savedLogin.equals("")
            )
        }
    }


    fun checkHealth() {
        viewModelScope.launch {
            _uiState.value = RecognitionUiState.Loading
            val result = api.healthCheck()

            _uiState.value = if (result.isSuccessful) {
                RecognitionUiState.HealthCheckSuccess
            } else {
                RecognitionUiState.Error(result.message() ?: "Unknown error")
            }
        }
    }

    fun registerPerson(name: String, base64Images: List<String>) {
        viewModelScope.launch {
            _registrationState.value = RegistrationUiState.Loading
            val result = api.registerPerson(RegisterRequest(name, base64Images))

            _registrationState.value = if (result.isSuccessful) {
                RegistrationUiState.Success(result.body() ?: emptyMap())
            } else {
                RegistrationUiState.Error(result.message() ?: "Registration failed")
            }
        }
    }

    fun recognizePerson(base64Image: String) {
        viewModelScope.launch {
            _uiState.value = RecognitionUiState.Loading
            val result = api.recognizePerson(RecognitionRequest(base64Image))

            _uiState.value = if (result.isSuccessful) {
                when (result.body()?.status) {
                    RecognitionStatus.SUCCESS -> {
                        RecognitionUiState.RecognitionSuccess(
                            name = result.body()!!.name ?: "Unknown",
                            similarity = result.body()!!.similarity ?: 0f
                        )
                    }

                    RecognitionStatus.MULTIPLE_FACES -> {
                        RecognitionUiState.Error("Multiple faces detected")
                    }

                    RecognitionStatus.NO_FACES -> {
                        RecognitionUiState.Error("No faces detected")
                    }

                    RecognitionStatus.NOT_REGISTERED -> {
                        RecognitionUiState.Error("Person not registered")
                    }

                    null -> RecognitionUiState.Error("Unknown status")
                }
            } else {
                RecognitionUiState.Error(result.message() ?: "Recognition failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = RecognitionUiState.Idle
        _registrationState.value = RegistrationUiState.Idle
    }

    fun retryConnection() {
        checkHealth()
    }

    fun clearMessages() {
        _uiLoginState.value = _uiLoginState.value.copy(
            showSuccessMessage = false,
            showResetMessage = false
        )
    }


    fun onLoginChange(newLogin: String) {
        _uiLoginState.value = _uiLoginState.value.copy(login = newLogin)
    }

    fun saveLogin() {
        viewModelScope.launch {
            val login = _uiLoginState.value.login.trim()
            if (login.isNotEmpty()) {
                userPreferences.saveLogin(login)
                _uiLoginState.value = _uiLoginState.value.copy(
                    isLoginSaved = true,
                    showSuccessMessage = true
                )
            }
        }

    }

    fun resetLogin() {
        viewModelScope.launch {
            userPreferences.clearLogin()
            _uiLoginState.value = LoginUiState(
                login = "",
                isLoginSaved = false,
                showResetMessage = true
            )
        }

    }
}


sealed class RecognitionUiState {
    object Idle : RecognitionUiState()
    object Loading : RecognitionUiState()
    object HealthCheckSuccess : RecognitionUiState()
    data class RecognitionSuccess(val name: String, val similarity: Float) : RecognitionUiState()
    data class Error(val message: String) : RecognitionUiState()
}

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    data class Success(val data: Map<String, Any>) : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()

}
